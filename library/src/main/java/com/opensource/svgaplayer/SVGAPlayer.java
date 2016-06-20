package com.opensource.svgaplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
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

    public void setVideoItem(SVGAVideoEntity videoItem) {
        this.videoItem = videoItem;
        this.invalidate();
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
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

    private void next() {
        if (this.videoItem.sprites.size() == 0) {
            return;
        }
        this.currentFrame++;
        if (this.currentFrame >= this.videoItem.sprites.get(0).frames.size()) {
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
            drawTransform.setTranslate((float)((this.videoWidth - videoItem.videoSize.width / 2.0) / 2.0), 0);
            drawTransform.setScale((float)(this.videoWidth / (videoItem.videoSize.width / 2.0)), (float)(this.videoWidth / (videoItem.videoSize.width / 2.0)));
            for (int i = 0; i < videoItem.sprites.size(); i++) {
                SVGAVideoSpriteEntity sprite = videoItem.sprites.get(i);
                SVGAVideoSpriteFrameEntity frame = sprite.frames.get(currentFrame);
                if (null != frame && frame.alpha > 0.0) {
                    BitmapDrawable bitmapDrawable = videoItem.images.get(sprite.sKey);
                    if (null != bitmapDrawable) {
                        Bitmap bitmap = bitmap(sprite.sKey, bitmapDrawable, frame.layout);
                        if (null != bitmap) {
                            Paint paint = new Paint();
                            paint.setAlpha((int) (frame.alpha * 255));
                            Matrix concatTransform = new Matrix();
                            concatTransform.setConcat(drawTransform, frame.transform);
                            canvas.drawBitmap(bitmap, concatTransform, paint);
                        }
                    }
                }

            }
        }
    }

    private Bitmap bitmap(String bitmapKey, BitmapDrawable bitmapDrawable, CGRect layout) {
        double imageWidth = bitmapDrawable.getIntrinsicWidth() * getResources().getDisplayMetrics().scaledDensity;
        double imageHeight = bitmapDrawable.getIntrinsicHeight() * getResources().getDisplayMetrics().scaledDensity;
        if (layout.width == imageWidth && layout.height == imageHeight) {
            Bitmap bitmap = videoItem.bitmapCache.get(bitmapKey);
            if (null == bitmap) {
                bitmap = bitmapDrawable.getBitmap();
                videoItem.bitmapCache.put(bitmapKey, bitmap);
            }
            return bitmap;
        }
        if (layout.width > 0 && layout.height > 0 && imageWidth > 0 && imageHeight > 0) {
            Bitmap bitmap = Bitmap.createBitmap((int)layout.width, (int)layout.height, Bitmap.Config.ARGB_8888);
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
            return bitmap;
        }
        else {
            return bitmapDrawable.getBitmap();
        }
    }

}
