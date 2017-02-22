package com.opensource.svgaplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.TextPaint;

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
    protected boolean paused = false;

    @Override
    public void run() {
        int fps = videoItem.FPS;
        boolean waiting = false;
        while (textureView.animating) {
            if (textureView.isAvailable()){
                synchronized (SVGADrawer.mLock) {
                    if (paused) {
                        try {
                            Thread.sleep((long) 1);
                        } catch (InterruptedException e) {}
                    }
                    else if (waiting) {
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
                    } else {
                        nextTimestamp = System.currentTimeMillis() + (1000 / fps);
                        currentCanvas = textureView.lockCanvas();
                        if (currentCanvas != null) {
                            drawFrame(currentCanvas);
                        }
                        waiting = true;
                    }
                }
            }
            else {
                synchronized (SVGADrawer.mLock){
                    try {
                        Thread.sleep((long) (1));
                    } catch (InterruptedException e) {}
                    if (!paused) {
                        stepFrame();
                    }
                }
            }
        }
    }

    void pause() {
        paused = true;
    }

    void restore() {
        paused = false;
    }

    void draw() {
        if (textureView.isAvailable()) {
            synchronized (SVGADrawer.mLock) {
                if (null != currentCanvas) {
                    textureView.unlockCanvasAndPost(currentCanvas);
                    currentCanvas = null;
                }
                currentCanvas = textureView.lockCanvas();
                if (currentCanvas != null) {
                    drawFrame(currentCanvas);
                    textureView.unlockCanvasAndPost(currentCanvas);
                    currentCanvas = null;
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
                textureView.stopDrawing(textureView.clearsAfterStop);
                if (null != textureView.callback) {
                    textureView.callback.onFinished(textureView);
                }
                textureView.releaseDrawer();
            }
        }
        if (textureView.callback != null && videoItem.frames > 0) {
            textureView.callback.onStep(textureView, currentFrame, (float) currentFrame / (float) videoItem.frames);
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
                        BitmapWithScale bitmap = scaleBitmap(bitmapDrawable, frame.layout);
                        if (null != bitmap) {
                            paint.setAlpha((int) (frame.alpha * 255));
                            concatTransform.setConcat(drawTransform, frame.transform);
                            if (bitmap.mScale > 0) {
                                concatTransform.preScale(bitmap.mScale, bitmap.mScale);
                            }
                            if (null != frame.maskPath) {
                                drawBitmap(canvas, bitmap.mBitmap, frame.maskPath, concatTransform);
                            }
                            else {
                                canvas.drawBitmap(bitmap.mBitmap, concatTransform, paint);
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
                                int fontMetricsBottom = (int)textPaint.getFontMetrics().bottom;
                                int fontMetricsTop = (int)textPaint.getFontMetrics().top;
                                int y = (targetRectBottom + targetRectTop - fontMetricsBottom - fontMetricsTop) / 2;
                                canvas.drawText(text, x, y, textPaint);
                            }
                        }
                    }
                    if (frame.shapes.length > 0) {
                        for (int j = 0; j < frame.shapes.length; j++) {
                            SVGAVideoShapeEntity shape = frame.shapes[j];
                            concatTransform.reset();
                            concatTransform.setConcat(drawTransform, frame.transform);
                            drawShape(canvas, shape, concatTransform);
                        }
                    }
                }

            }
        }
    }

    private Paint maskPaint = new Paint();

    private void drawBitmap(Canvas canvas, Bitmap imageBitmap, Path maskPath, Matrix matrix) {
        canvas.save();
        canvas.setMatrix(matrix);
        canvas.clipRect(0, 0, imageBitmap.getWidth(), imageBitmap.getHeight());
        BitmapShader bitmapShader = new BitmapShader(imageBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        maskPaint.setShader(bitmapShader);
        canvas.drawPath(maskPath, maskPaint);
        canvas.restore();
    }

    static class BitmapWithScale {
        final Bitmap mBitmap;
        final float mScale;
        BitmapWithScale(Bitmap bitmap) {
            this(bitmap, -1f);
        }
        BitmapWithScale(Bitmap bitmap, float scale) {
            mBitmap = bitmap;
            mScale = scale;
        }
    }

    private BitmapWithScale scaleBitmap(BitmapDrawable bitmapDrawable, SVGARect layout) {
        Bitmap bitmap = bitmapDrawable.getBitmap();
        if (Math.abs(layout.width - bitmap.getWidth()) < 0.01f
                && Math.abs(layout.height - bitmap.getHeight()) < 0.01f) {
            return new BitmapWithScale(bitmap);
        }
        if (layout.width > 0 && layout.height > 0) {
            double scale = layout.width / bitmap.getWidth();
            return new BitmapWithScale(bitmap, (float) scale);
        } else {
            return new BitmapWithScale(bitmapDrawable.getBitmap());
        }
    }

    private void drawShape(Canvas canvas, SVGAVideoShapeEntity shape, Matrix drawTransform) {
        Path finalPath = new Path();
        if (shape.type == SVGAVideoShapeEntity.Type.shape) {
            Object d = shape.args.get("d");
            if (d instanceof String) {
                SVGAPath svgaPath = new SVGAPath();
                svgaPath.setValues((String) d);
                finalPath = svgaPath.getPath();
            }
        }
        else if (shape.type == SVGAVideoShapeEntity.Type.ellipse) {
            Object xv = shape.args.get("x");
            Object yv = shape.args.get("y");
            Object rxv = shape.args.get("radiusX");
            Object ryv = shape.args.get("radiusY");
            if (xv instanceof Number && yv instanceof Number && rxv instanceof Number && ryv instanceof Number) {
                float x = ((Number) xv).floatValue();
                float y = ((Number) yv).floatValue();
                float rx = ((Number) rxv).floatValue();
                float ry = ((Number) ryv).floatValue();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finalPath.addOval(x - rx, y - ry, x + rx, y + ry, Path.Direction.CW);
                }
                else if (Math.abs(rx - ry) < 0.1) {
                    finalPath.addCircle(x, y, rx, Path.Direction.CW);
                }
            }
        }
        else if (shape.type == SVGAVideoShapeEntity.Type.rect) {
            Object xv = shape.args.get("x");
            Object yv = shape.args.get("y");
            Object wv = shape.args.get("width");
            Object hv = shape.args.get("height");
            Object crv = shape.args.get("cornerRadius");
            if (xv instanceof Number && yv instanceof Number && wv instanceof Number && hv instanceof Number && crv instanceof Number) {
                float x = ((Number) xv).floatValue();
                float y = ((Number) yv).floatValue();
                float width = ((Number) wv).floatValue();
                float height = ((Number) hv).floatValue();
                float cornerRadius = ((Number) crv).floatValue();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finalPath.addRoundRect(x, y, x + width, y + height, cornerRadius, cornerRadius, Path.Direction.CW);
                }
                else {
                    finalPath.addRect(x, y, x + width, y + height, Path.Direction.CW);
                }
            }
        }
        if (finalPath != null) {
            Matrix thisTransform = new Matrix();
            thisTransform.postConcat(shape.transform);
            thisTransform.postConcat(drawTransform);
            finalPath.transform(thisTransform);
            if (shape.styles.fill != 0x00000000) {
                paint.reset();
                paint.setAntiAlias(true);
                paint.setColor(shape.styles.fill);
                canvas.drawPath(finalPath, paint); // draw fill
            }
            if (shape.styles.strokeWidth > 0) {
                resetShapeStrokePaint(shape);
                canvas.drawPath(trimmedPath(finalPath, shape.styles.trimStart, shape.styles.trimEnd), paint); // draw stroke
            }
        }
    }

    private Path trimmedPath(Path path, float start, float end) {
        if (Math.abs(start - 0.0) < 0.01 && Math.abs(end - 1.0) < 0.01) {
            return path;
        }
        else {
            Path currentPath = new Path(path);
            Path tmpPath = new Path();
            tmpPath.set(currentPath);
            PathMeasure pathMeasure = new PathMeasure();
            pathMeasure.setPath(tmpPath, false);
            float length = pathMeasure.getLength();
            float _start = length * start;
            float _end = length * end;
            float newStart = Math.min(_start, _end);
            float newEnd = Math.max(_start, _end);
            currentPath.reset();
            if (newStart > length && newEnd > length) {
                newStart %= length;
                newEnd %= length;
            }
            if (newStart > newEnd) {
                newStart -= length;
            }
            pathMeasure.getSegment(newStart, newEnd, currentPath, true);
            return currentPath;
        }
    }

    private void resetShapeStrokePaint(SVGAVideoShapeEntity shape) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(shape.styles.stroke);
        paint.setStrokeWidth(shape.styles.strokeWidth);
        if (shape.styles.lineCap.equalsIgnoreCase("butt")) {
            paint.setStrokeCap(Paint.Cap.BUTT);
        }
        else if (shape.styles.lineCap.equalsIgnoreCase("round")) {
            paint.setStrokeCap(Paint.Cap.ROUND);
        }
        else if (shape.styles.lineCap.equalsIgnoreCase("square")) {
            paint.setStrokeCap(Paint.Cap.SQUARE);
        }
        if (shape.styles.lineJoin.equalsIgnoreCase("miter")) {
            paint.setStrokeJoin(Paint.Join.MITER);
        }
        else if (shape.styles.lineJoin.equalsIgnoreCase("round")) {
            paint.setStrokeJoin(Paint.Join.ROUND);
        }
        else if (shape.styles.lineJoin.equalsIgnoreCase("bevel")) {
            paint.setStrokeJoin(Paint.Join.BEVEL);
        }
        paint.setStrokeMiter(shape.styles.miterLimit);
        if (shape.styles.lineDash.length == 3) {
            paint.setPathEffect(new DashPathEffect(new float[] {
                    shape.styles.lineDash[0] < 1f ? 1f : shape.styles.lineDash[0],
                    shape.styles.lineDash[1] < 0.1f ? 0.1f : shape.styles.lineDash[1]
                    }, shape.styles.lineDash[2])
            );
        }
    }

}
