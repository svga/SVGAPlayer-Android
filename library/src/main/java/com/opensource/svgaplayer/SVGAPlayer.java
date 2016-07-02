package com.opensource.svgaplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Date;
import java.util.HashMap;

interface SVGAPlayerDelegate {

    void svgaPlayerDidFinishedAnimation(SVGAPlayer player);

}

/**
 * Created by PonyCui_Home on 16/6/19.
 */
public class SVGAPlayer extends SurfaceView implements SurfaceHolder.Callback {

    public SVGAPlayerDelegate delegate;
    public int loops = 0;
    public boolean clearsAfterStop = true;
    public boolean keepRate = true;

    private SVGADrawer drawer = new SVGADrawer();

    public SVGAPlayer(Context context) {
        super(context);
        this.drawer.playerInstance = this;
        this.drawer.holder = this.getHolder();
        this.drawer.holder.addCallback(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.drawer.videoWidth = (int) (getWidth() / getResources().getDisplayMetrics().scaledDensity);
        this.drawer.scaledDensity = getResources().getDisplayMetrics().scaledDensity;
    }

    public void setVideoItem(SVGAVideoEntity videoItem) {
        this.drawer.videoItem = videoItem;
    }

    public void startAnimation() {
        this.drawer.startAnimating();
    }

    public void stopAnimation() {
        this.drawer.stopAnimating();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (this.drawer.isAnimating()) {
            this.drawer.startAnimating();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.drawer.stopAnimating();
    }

}

class SVGADrawer extends Thread {

    SVGAPlayer playerInstance;
    SVGAVideoEntity videoItem;
    SurfaceHolder holder;
    int videoWidth;
    float scaledDensity;

    private boolean animating = false;
    private int loopCount = 0;
    private int currentFrame = 0;
    private long nextTimestamp = 0;
    private int frameRate = 1;

    public boolean isAnimating() {
        return animating;
    }

    public void startAnimating() {
        animating = true;
        start();
    }

    public void stopAnimating() {
        animating = false;
        if (playerInstance.clearsAfterStop) {
            Canvas canvas = holder.lockCanvas();
            if(canvas != null) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void run() {
        if (null == videoItem) {
            return;
        }
        Canvas canvas = null;
        boolean waiting = false;
        while (animating) {
            synchronized (holder) {
                if (waiting) {
                    if ((System.currentTimeMillis()) < nextTimestamp) {
                        try {
                            this.sleep((long) 1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    if (null != canvas) {
                        holder.unlockCanvasAndPost(canvas);
                    }
                    waiting = false;
                    for (int i = 0; i < frameRate; i++) {
                        stepFrame();
                    }
                }
                else {
                    int FPS = videoItem.FPS;
                    FPS = FPS / frameRate;
                    nextTimestamp = System.currentTimeMillis() + (1000 / FPS);
                    canvas = holder.lockCanvas();
                    drawFrame(canvas);
                    waiting = true;
                    if (!playerInstance.keepRate && System.currentTimeMillis() > nextTimestamp) {
                        frameRate = frameRate + 1;
                        System.out.println("SVGA frameRate = " + frameRate);
                    }
                }
            }
        }
    }

    private void stepFrame() {
        if (this.videoItem.sprites.size() == 0) {
            return;
        }
        this.currentFrame++;
        if (this.currentFrame >= this.videoItem.frames) {
            this.currentFrame = 0;
            loopCount++;
            if (playerInstance.loops > 0 && loopCount >= playerInstance.loops) {
                stopAnimating();
                if (null != playerInstance.delegate) {
                    playerInstance.delegate.svgaPlayerDidFinishedAnimation(playerInstance);
                }
            }
        }
    }
    Matrix drawTransform = new Matrix();
    Paint paint = new Paint();
    Matrix concatTransform = new Matrix();

    private void drawFrame(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (null != videoItem) {
            drawTransform.setScale((float) (this.videoWidth / (videoItem.videoSize.width / scaledDensity)), (float) (this.videoWidth / (videoItem.videoSize.width / scaledDensity)));
            for (int i = 0; i < videoItem.sprites.size(); i++) {
                SVGAVideoSpriteEntity sprite = videoItem.sprites.get(i);
                SVGAVideoSpriteFrameEntity frame = sprite.frames.get(currentFrame);
                if (null != frame && frame.alpha > 0.0) {
                    BitmapDrawable bitmapDrawable = videoItem.images.get(sprite.imageKey);
                    if (null != bitmapDrawable) {
                        Bitmap bitmap = bitmap(sprite.imageKey, bitmapDrawable, frame.layout);
                        if (null != bitmap) {
                            paint.setAlpha((int) (frame.alpha * 255));
                            if (null != frame.maskPath) {
                                bitmap = bitmap(bitmap, frame.maskPath);
                            }
                            concatTransform.setConcat(drawTransform, frame.transform);
                            canvas.drawBitmap(bitmap, concatTransform, paint);
                        }
                    }
                }

            }
        }
    }

    private Bitmap bitmap(Bitmap imageBitmap, Path maskPath) {
        Bitmap outBitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outBitmap);
        Paint maskPaint = new Paint();
        BitmapShader bitmapShader = new BitmapShader(imageBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        maskPaint.setShader(bitmapShader);
        canvas.drawPath(maskPath, maskPaint);
        return outBitmap;
    }

    private Bitmap bitmap(String bitmapKey, BitmapDrawable bitmapDrawable, CGRect layout) {
        double imageWidth = bitmapDrawable.getIntrinsicWidth() * scaledDensity;
        double imageHeight = bitmapDrawable.getIntrinsicHeight() * scaledDensity;
        String bitmapCacheKey = bitmapKey + "." + String.valueOf((int)layout.width) + "." + String.valueOf((int)layout.height);
        if (layout.width == imageWidth && layout.height == imageHeight) {
            Bitmap bitmap = videoItem.bitmapCache.get(bitmapCacheKey);
            if (null == bitmap) {
                bitmap = bitmapDrawable.getBitmap();
                videoItem.bitmapCache.put(bitmapCacheKey, bitmap);
            }
            return bitmap;
        }
        if (layout.width > 0 && layout.height > 0 && imageWidth > 0 && imageHeight > 0) {
            Bitmap bitmap = videoItem.bitmapCache.get(bitmapCacheKey);
            if (null != bitmap) {
                return bitmap;
            }
            bitmap = Bitmap.createBitmap((int)layout.width, (int)layout.height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            if (layout.width / layout.height < imageWidth / imageHeight) {
                // width > height
                double ratio = layout.width / imageWidth;
                double top = (layout.height - imageHeight * ratio) / 2.0;
                bitmapDrawable.setBounds(0, (int)top, (int)layout.width, (int)(layout.height - top));
                bitmapDrawable.draw(canvas);
            }
            else {
                // height > width
                double ratio = layout.height / imageHeight;
                double left = (layout.width - imageWidth * ratio) / 2.0;
                bitmapDrawable.setBounds((int)left, 0, (int)(layout.width - left), (int)layout.height);
                bitmapDrawable.draw(canvas);
            }
            videoItem.bitmapCache.put(bitmapCacheKey, bitmap);
            return bitmap;
        }
        else {
            return bitmapDrawable.getBitmap();
        }
    }

}