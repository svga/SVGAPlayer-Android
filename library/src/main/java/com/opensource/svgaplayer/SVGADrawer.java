package com.opensource.svgaplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextPaint;
import android.view.TextureView;

/**
 * Created by cuiminghui on 2016/10/17.
 */
class SVGADrawer implements Runnable {

    private static final String TAG = "SVGADrawer";
    protected static Object mLock = new Object();
    protected SVGAPlayer textureView;
    protected SVGAVideoEntity videoItem;
    protected int videoWidth;
    protected float scaledDensity;
    protected Canvas currentCanvas;
    protected int loopCount = 0;
    protected int currentFrame = 0;
    protected long nextTimestamp = 0;

    @Override
    public void run() {
        boolean waiting = false;
        while (textureView.animating && textureView.isAvailable()) {
            synchronized (SVGADrawer.mLock) {
                if (waiting) {
                    if ((System.currentTimeMillis()) < nextTimestamp) {
                        try {
                            Thread.sleep((long) 1);
                        } catch (InterruptedException e) {}
                        continue;
                    }
                    if (null != currentCanvas) {
                        textureView.unlockCanvasAndPost(currentCanvas);
                        currentCanvas = null;
                    }
                    waiting = false;
                    stepFrame();
                }
                else {
                    int FPS = videoItem.FPS;
                    nextTimestamp = System.currentTimeMillis() + (1000 / FPS);
                    currentCanvas = textureView.lockCanvas();
                    if (currentCanvas != null) {
                        drawFrame(currentCanvas);
                    }
                    waiting = true;
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
            if (textureView.loops > 0 && loopCount >= textureView.loops) {
                textureView.animating = false;
                textureView.stopDrawing();
                if (null != textureView.callback) {
                    textureView.callback.onFinished(textureView);
                }
                textureView.releaseDrawer();
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
                    if (textureView.dynamicImages.containsKey(sprite.imageKey)) {
                        bitmapDrawable = textureView.dynamicImages.get(sprite.imageKey);
                    }
                    if (null != bitmapDrawable) {
                        Bitmap bitmap = bitmap(sprite.imageKey, bitmapDrawable, frame.layout);
                        if (null != bitmap) {
                            paint.setAlpha((int) (frame.alpha * 255));
                            concatTransform.setConcat(drawTransform, frame.getTransform());

                            if (null != frame.getMaskPath()) {
                                bitmap(canvas, bitmap, frame.getMaskPath(), concatTransform);
                            }
                            else {
                                canvas.drawBitmap(bitmap, concatTransform, paint);
                            }
                            if (textureView.dynamicTexts.containsKey(sprite.imageKey)) {
                                String text = textureView.dynamicTexts.get(sprite.imageKey);
                                TextPaint textPaint = textureView.dynamicTextPaints.get(sprite.imageKey);
                                textPaint.setAlpha(paint.getAlpha());
                                float[] values = new float[9];
                                concatTransform.getValues(values);
                                Rect bounds = new Rect();
                                textPaint.getTextBounds(text, 0, text.length(), bounds);
                                int x = (int)(values[2] + ((values[0] * frame.layout.width - bounds.width()) / 2.0));
                                int targetRectTop = (int)(values[5]);
                                int targetRectBottom = (int)(values[5] + values[4] * frame.layout.height);
                                int fonrMetricsBottom = (int)textPaint.getFontMetrics().bottom;
                                int fonrMetricsTop = (int)textPaint.getFontMetrics().top;
                                int y = (targetRectBottom + targetRectTop - fonrMetricsBottom - fonrMetricsTop) / 2;
                                canvas.drawText(text, x, y, textPaint);
                            }
                        }
                    }
                }

            }
        }
    }

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

    private Bitmap bitmap(String bitmapKey, BitmapDrawable bitmapDrawable, SVGARect layout) {
        double imageWidth = bitmapDrawable.getIntrinsicWidth() * scaledDensity;
        double imageHeight = bitmapDrawable.getIntrinsicHeight() * scaledDensity;

        SVGABitmapCacheKey SVGABitmapCacheKey = new SVGABitmapCacheKey(bitmapKey, (int) layout.width, (int) layout.height);

        if (layout.width == imageWidth && layout.height == imageHeight) {
            Bitmap bitmap = videoItem.bitmapCache.get(SVGABitmapCacheKey);
            if (null == bitmap) {
                bitmap = bitmapDrawable.getBitmap();
                videoItem.bitmapCache.put(SVGABitmapCacheKey, bitmap);
            }
            bitmap.prepareToDraw();
            return bitmap;
        }
        if (layout.width > 0 && layout.height > 0 && imageWidth > 0 && imageHeight > 0) {
            Bitmap bitmap = videoItem.bitmapCache.get(SVGABitmapCacheKey);
            if (null != bitmap) {
                bitmap.prepareToDraw();
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
            videoItem.bitmapCache.put(SVGABitmapCacheKey, bitmap);
            return bitmap;
        } else {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap.prepareToDraw();
            return bitmap;
        }
    }

}
