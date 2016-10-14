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
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import java.util.HashMap;

interface SVGAPlayerDelegate {

    void svgaPlayerDidFinishedAnimation(SVGAPlayer player);

}

/**
 * Created by PonyCui_Home on 16/6/19.
 */
public class SVGAPlayer extends TextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = "SVGAPlayer";
    public HashMap<String, BitmapDrawable> dynamicImages = new HashMap<>();
    public SVGAPlayerDelegate delegate;
    public int loops = 0;
    public boolean clearsAfterStop = true;
    public boolean keepRate = true;

    private SVGADrawer drawer = new SVGADrawer();

    public SVGAPlayer(Context context) {
        super(context);
        init();
    }

    public SVGAPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SVGAPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.drawer.playerInstance = this;
        this.drawer.textureView = this;
        this.setSurfaceTextureListener(this);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setOpaque(false);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            this.drawer.videoWidth = (int) (getWidth() / getResources().getDisplayMetrics().scaledDensity);
            this.drawer.scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        }
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

    public void setDynamicImage(BitmapDrawable drawable, String forKey) {
        this.dynamicImages.put(forKey, drawable);
    }

    public void clearDynamicObjects() {
        this.dynamicImages.clear();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.drawer.videoWidth = (int) (getWidth() / getResources().getDisplayMetrics().scaledDensity);
        this.drawer.scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        if (this.drawer.isAnimating()) {
            this.drawer.startAnimating();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        this.drawer.videoWidth = (int) (getWidth() / getResources().getDisplayMetrics().scaledDensity);
        this.drawer.scaledDensity = getResources().getDisplayMetrics().scaledDensity;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        this.drawer.stopAnimating();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}

class SVGADrawer extends Thread {

    SVGAPlayer playerInstance;
    SVGAVideoEntity videoItem;
    TextureView textureView;
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
            Canvas canvas = textureView.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                textureView.unlockCanvasAndPost(canvas);
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
            synchronized (textureView) {
                if (waiting) {
                    if ((System.currentTimeMillis()) < nextTimestamp) {
                        try {
                            sleep((long) 1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    if (null != canvas) {
                        textureView.unlockCanvasAndPost(canvas);
                    }
                    waiting = false;
                    for (int i = 0; i < frameRate; i++) {
                        stepFrame();
                    }
                } else {
                    int FPS = videoItem.FPS;
                    FPS = FPS / frameRate;
                    nextTimestamp = System.currentTimeMillis() + (1000 / FPS);
                    canvas = textureView.lockCanvas();
                    if (canvas != null) {
                        drawFrame(canvas);
                    }
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
            drawTransform.setScale(
                    (float) (this.videoWidth / (videoItem.videoSize.width / scaledDensity)),
                    (float) (this.videoWidth / (videoItem.videoSize.width / scaledDensity)));

            for (int i = 0; i < videoItem.sprites.size(); i++) {
                SVGAVideoSpriteEntity sprite = videoItem.sprites.get(i);
                SVGAVideoSpriteFrameEntity frame = sprite.frames.get(currentFrame);
                if (null != frame && frame.alpha > 0.0) {
                    BitmapDrawable bitmapDrawable = videoItem.images.get(sprite.imageKey);
                    if (this.playerInstance.dynamicImages.containsKey(sprite.imageKey)) {
                        bitmapDrawable = this.playerInstance.dynamicImages.get(sprite.imageKey);
                    }
                    if (null != bitmapDrawable) {
                        Bitmap bitmap = bitmap(sprite.imageKey, bitmapDrawable, frame.layout);
                        if (null != bitmap) {
                            paint.setAlpha((int) (frame.alpha * 255));
                            concatTransform.setConcat(drawTransform, frame.getTransform());

                            if (null != frame.getMaskPath()) {
                                bitmap(canvas, bitmap, frame.getMaskPath(), concatTransform);
                            } else {
                                canvas.drawBitmap(bitmap, concatTransform, paint);
                            }

                        }
                    }
                }

            }
        }
    }

    private static final String TAG = "SVGADrawer";

    private Paint maskPaint = new Paint();

    private void bitmap(Canvas canvas, Bitmap imageBitmap, Path maskPath, Matrix matrix) {
        canvas.save();
        canvas.setMatrix(matrix);
        canvas.clipRect(0, 0, imageBitmap.getWidth(), imageBitmap.getHeight());
        BitmapShader bitmapShader = new BitmapShader(imageBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        maskPaint.setShader(bitmapShader);
        canvas.drawPath(maskPath, maskPaint);
        canvas.restore();
    }

    private Bitmap bitmap(String bitmapKey, BitmapDrawable bitmapDrawable, CGRect layout) {
        double imageWidth = bitmapDrawable.getIntrinsicWidth() * scaledDensity;
        double imageHeight = bitmapDrawable.getIntrinsicHeight() * scaledDensity;

        BitmapCacheKey bitmapCacheKey = new BitmapCacheKey(bitmapKey, (int) layout.width, (int) layout.height);

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
            bitmap = Bitmap.createBitmap((int) layout.width, (int) layout.height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            if (layout.width / layout.height < imageWidth / imageHeight) {
                // width > height
                double ratio = layout.width / imageWidth;
                double top = (layout.height - imageHeight * ratio) / 2.0;
                bitmapDrawable.setBounds(0, (int) top, (int) layout.width, (int) (layout.height - top));
                bitmapDrawable.draw(canvas);
            } else {
                // height > width
                double ratio = layout.height / imageHeight;
                double left = (layout.width - imageWidth * ratio) / 2.0;
                bitmapDrawable.setBounds((int) left, 0, (int) (layout.width - left), (int) layout.height);
                bitmapDrawable.draw(canvas);
            }
            videoItem.bitmapCache.put(bitmapCacheKey, bitmap);
            return bitmap;
        } else {
            return bitmapDrawable.getBitmap();
        }
    }

}