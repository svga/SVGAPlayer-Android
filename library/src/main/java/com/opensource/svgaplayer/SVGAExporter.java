package com.opensource.svgaplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;

import java.io.ByteArrayOutputStream;

/**
 * Created by cuiminghui on 2017/3/8.
 */

public class SVGAExporter {

    public SVGAVideoEntity videoItem;

    public ByteArrayOutputStream[] toPNGByteArray() {
        ByteArrayOutputStream[] images = new ByteArrayOutputStream[videoItem.frames];
        for (int i = 0; i < videoItem.frames; i++) {
            currentFrame = i;
            Bitmap bitmap = Bitmap.createBitmap((int)videoItem.videoSize.width, (int)videoItem.videoSize.height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawFrame(canvas);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 1, byteArrayOutputStream);
            images[i] = byteArrayOutputStream;
            bitmap.recycle();
        }
        return images;
    }

    protected int currentFrame = 0;

    Matrix drawTransform = new Matrix();
    Paint paint = new Paint();
    Matrix concatTransform = new Matrix();

    protected void drawFrame(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (null != videoItem) {
            drawTransform.setScale(1.0f, 1.0f);
            for (int i = 0; i < videoItem.sprites.size(); i++) {
                SVGAVideoSpriteEntity sprite = videoItem.sprites.get(i);
                SVGAVideoSpriteFrameEntity frame = sprite.frames.get(currentFrame);
                if (null != frame && frame.alpha > 0.0) {
                    BitmapDrawable bitmapDrawable = videoItem.images.get(sprite.imageKey);
                    if (null != bitmapDrawable) {
                        SVGADrawer.BitmapWithScale bitmap = scaleBitmap(bitmapDrawable, frame.layout);
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

    private SVGADrawer.BitmapWithScale scaleBitmap(BitmapDrawable bitmapDrawable, SVGARect layout) {
        Bitmap bitmap = bitmapDrawable.getBitmap();
        if (Math.abs(layout.width - bitmap.getWidth()) < 0.01f
                && Math.abs(layout.height - bitmap.getHeight()) < 0.01f) {
            return new SVGADrawer.BitmapWithScale(bitmap);
        }
        if (layout.width > 0 && layout.height > 0) {
            double scale = layout.width / bitmap.getWidth();
            return new SVGADrawer.BitmapWithScale(bitmap, (float) scale);
        } else {
            return new SVGADrawer.BitmapWithScale(bitmapDrawable.getBitmap());
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
                canvas.drawPath(finalPath, paint); // draw stroke
            }
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
