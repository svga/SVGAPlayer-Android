package com.opensource.svgaplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.Choreographer;
import android.view.View;

interface SVGAPlayerDelegate {

    void svgaPlayerDidFinishedAnimation(SVGAPlayer player);

}

/**
 * Created by PonyCui_Home on 16/6/19.
 */
public class SVGAPlayer extends View implements Choreographer.FrameCallback {

    private SVGAVideoEntity videoItem;
    private int videoWidth = 375;
    public SVGAPlayerDelegate delegate;
    public int loops = 0;
    public boolean clearsAfterStop = true;


    public SVGAPlayer(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setVideoWidth((int) (getWidth() / getResources().getDisplayMetrics().scaledDensity));
    }

    public void setVideoItem(SVGAVideoEntity videoItem) {
        this.videoItem = videoItem;
        this.invalidate();
    }

    public void startAnimation() {
        animating = true;
        loopCount = 0;
        doFrame(0);
    }

    public void stopAnimation() {
        animating = false;
        this.invalidate();
    }

    private boolean animating = false;
    private int loopCount = 0;
    private int currentFrame = 0;
    private int skipCount = 0;

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!animating) {
            return;
        }
        this.skipCount++;
        if (this.skipCount >= 60 / this.videoItem.FPS) {
            this.skipCount = 0;
            this.next();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Choreographer.getInstance().postFrameCallback(this);
        }
    }

    private void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
        this.invalidate();
    }

    private void next() {
        if (this.videoItem.sprites.size() == 0) {
            return;
        }
        this.currentFrame++;
        if (this.currentFrame >= this.videoItem.frames) {
            this.currentFrame = 0;
            loopCount++;
            if (loops > 0 && loopCount >= loops) {
                stopAnimation();
                if (null != delegate) {
                    delegate.svgaPlayerDidFinishedAnimation(this);
                }
            }
        }
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (clearsAfterStop && !animating) {
            return;
        }
        if (null != videoItem) {
            Matrix drawTransform = new Matrix();
            drawTransform.setScale((float) (this.videoWidth / (videoItem.videoSize.width / getResources().getDisplayMetrics().scaledDensity)), (float) (this.videoWidth / (videoItem.videoSize.width / getResources().getDisplayMetrics().scaledDensity)));
            for (int i = 0; i < videoItem.sprites.size(); i++) {
                SVGAVideoSpriteEntity sprite = videoItem.sprites.get(i);
                SVGAVideoSpriteFrameEntity frame = sprite.frames.get(currentFrame);
                if (null != frame && frame.alpha > 0.0) {
                    BitmapDrawable bitmapDrawable = videoItem.images.get(sprite.imageKey);
                    if (null != bitmapDrawable) {
                        Bitmap bitmap = bitmap(sprite.imageKey, bitmapDrawable, frame.layout);
                        if (null != bitmap) {
                            Paint paint = new Paint();
                            paint.setAlpha((int) (frame.alpha * 255));
                            if (null != frame.maskPath) {
                                bitmap = bitmap(bitmap, frame.maskPath);
                            }
                            Matrix concatTransform = new Matrix();
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
        maskPaint.setColor(Color.WHITE);
        Paint imagePaint = new Paint();
        imagePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawPath(maskPath, maskPaint);
        canvas.drawBitmap(imageBitmap, 0, 0, imagePaint);
        return outBitmap;
    }

    private Bitmap bitmap(String bitmapKey, BitmapDrawable bitmapDrawable, CGRect layout) {
        double imageWidth = bitmapDrawable.getIntrinsicWidth() * getResources().getDisplayMetrics().scaledDensity;
        double imageHeight = bitmapDrawable.getIntrinsicHeight() * getResources().getDisplayMetrics().scaledDensity;
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
