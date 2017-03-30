package com.opensource.svgaplayer

import android.graphics.*
import android.os.Build
import android.R.attr.x



/**
 * Created by cuiminghui on 2017/3/29.
 */

class SVGACanvasDrawer(videoItem: SVGAVideoEntity, val dynamicItem: SVGADynamicEntity, val canvas: Canvas) : SGVADrawer(videoItem) {

    val sharedPaint = Paint()
    val sharedContentTransform = Matrix()

    override fun drawFrame(frameIndex: Int) {
        super.drawFrame(frameIndex)
        val sprites = requestFrameSprites(frameIndex)
        sprites.forEach {
            drawSprite(it)
        }
    }

    private fun drawSprite(sprite: SVGADrawerSprite) {
        drawImage(sprite)
        drawShape(sprite)
    }

    private fun drawImage(sprite: SVGADrawerSprite) {
        (dynamicItem.dynamicImage[sprite.imageKey] ?: videoItem.images[sprite.imageKey])?.let {
            val drawingBitmap = it
            sharedPaint.reset()
            sharedContentTransform.reset()
            sharedPaint.alpha = (sprite.frameEntity.alpha * 255).toInt()
            sharedContentTransform.setScale((canvas.width / videoItem.videoSize.width).toFloat(), (canvas.width / videoItem.videoSize.width).toFloat())
            sharedContentTransform.preConcat(sprite.frameEntity.transform)
            sharedContentTransform.preScale((sprite.frameEntity.layout.width / drawingBitmap.width).toFloat(), (sprite.frameEntity.layout.width / drawingBitmap.width).toFloat())
            if (sprite.frameEntity.maskPath != null) {
                val maskPath = sprite.frameEntity.maskPath ?: return@let
                canvas.save()
                canvas.concat(sharedContentTransform)
                canvas.clipRect(0, 0, drawingBitmap.width, drawingBitmap.height)
                val bitmapShader = BitmapShader(drawingBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                sharedPaint.shader = bitmapShader
                canvas.drawPath(maskPath, sharedPaint)
                canvas.restore()
            }
            else {
                canvas.drawBitmap(drawingBitmap, sharedContentTransform, sharedPaint)
            }
            drawText(drawingBitmap, sprite)
        }
    }

    private fun drawText(drawingBitmap: Bitmap, sprite: SVGADrawerSprite) {
        dynamicItem.dynamicText[sprite.imageKey]?.let {
            val drawingText = it
            dynamicItem.dynamicTextPaint[sprite.imageKey]?.let {
                val drawingTextPaint = it
                val textBitmap = Bitmap.createBitmap(drawingBitmap.width, drawingBitmap.height, Bitmap.Config.ARGB_8888)
                val textCanvas = Canvas(textBitmap)
                drawingTextPaint.isAntiAlias = true
                val bounds = Rect()
                drawingTextPaint.getTextBounds(drawingText, 0, drawingText.length, bounds)
                val x = (drawingBitmap.width - bounds.width()) / 2.0
                val targetRectTop = 0
                val targetRectBottom = drawingBitmap.height
                val y = (targetRectBottom + targetRectTop - drawingTextPaint.fontMetrics.bottom - drawingTextPaint.fontMetrics.top) / 2
                textCanvas.drawText(drawingText, x.toFloat(), y, drawingTextPaint)
                if (sprite.frameEntity.maskPath != null) {
                    val maskPath = sprite.frameEntity.maskPath ?: return@let
                    canvas.save()
                    canvas.concat(sharedContentTransform)
                    canvas.clipRect(0, 0, drawingBitmap.width, drawingBitmap.height)
                    val bitmapShader = BitmapShader(textBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                    sharedPaint.shader = bitmapShader
                    canvas.drawPath(maskPath, sharedPaint)
                    canvas.restore()
                }
                else {
                    canvas.drawBitmap(textBitmap, sharedContentTransform, sharedPaint)
                }
            }
        }
    }

    private fun drawShape(sprite: SVGADrawerSprite) {
        sharedContentTransform.reset()
        sharedContentTransform.setScale((canvas.width / videoItem.videoSize.width).toFloat(), (canvas.width / videoItem.videoSize.width).toFloat())
        sharedContentTransform.preConcat(sprite.frameEntity.transform)
        sprite.frameEntity.shapes.forEach {
            val shape = it
            var finalPath = Path()
            if (shape.type == SVGAVideoShapeEntity.Type.shape) {
                (shape.args?.get("d") as? String)?.let {
                    SVGAPath(it).path?.let {
                        finalPath = it
                    }
                }
            }
            else if (shape.type == SVGAVideoShapeEntity.Type.ellipse) {
                val xv = shape.args?.get("x") as? Number ?: return
                val yv = shape.args?.get("y") as? Number ?: return
                val rxv = shape.args?.get("radiusX") as? Number ?: return
                val ryv = shape.args?.get("radiusY") as? Number ?: return
                val x = xv.toFloat()
                val y = yv.toFloat()
                val rx = rxv.toFloat()
                val ry = ryv.toFloat()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finalPath.addOval(x - rx, y - ry, x + rx, y + ry, Path.Direction.CW)
                }
                else if (Math.abs(rx - ry) < 0.1) {
                    finalPath.addCircle(x, y, rx, Path.Direction.CW)
                }
            }
            else if (shape.type == SVGAVideoShapeEntity.Type.rect) {
                val xv = shape.args?.get("x") as? Number ?: return
                val yv = shape.args?.get("y") as? Number ?: return
                val wv = shape.args?.get("width") as? Number ?: return
                val hv = shape.args?.get("height") as? Number ?: return
                val crv = shape.args?.get("cornerRadius") as? Number ?: return
                val x = xv.toFloat()
                val y = yv.toFloat()
                val width = wv.toFloat()
                val height = hv.toFloat()
                val cornerRadius = crv.toFloat()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finalPath.addRoundRect(x, y, x + width, y + height, cornerRadius, cornerRadius, Path.Direction.CW)
                }
                else {
                    finalPath.addRect(x, y, x + width, y + height, Path.Direction.CW)
                }
            }
            if (finalPath != null) {
                val thisTransform = Matrix()
                shape.transform?.let {
                    thisTransform.postConcat(it)
                }
                thisTransform.postConcat(sharedContentTransform)
                finalPath.transform(thisTransform)
                shape.styles?.fill?.let {
                    if (it != 0x00000000) {
                        sharedPaint.reset()
                        sharedPaint.color = it
                        sharedPaint.isAntiAlias = true
                        canvas.drawPath(finalPath, sharedPaint)
                    }
                }
                shape.styles?.strokeWidth?.let {
                    if (it > 0) {
                        sharedPaint.reset()
                        resetShapeStrokePaint(shape)
                        canvas.drawPath(finalPath, sharedPaint)
                    }
                }
            }
        }
    }

    private fun resetShapeStrokePaint(shape: SVGAVideoShapeEntity) {
        sharedPaint.reset()
        sharedPaint.isAntiAlias = true
        sharedPaint.style = Paint.Style.STROKE
        shape.styles?.stroke?.let {
            sharedPaint.color = it
        }
        shape.styles?.strokeWidth?.let {
            sharedPaint.strokeWidth = it
        }
        shape.styles?.lineCap?.let {
            if (it.equals("butt", true)) {
                sharedPaint.strokeCap = Paint.Cap.BUTT
            }
            else if (it.equals("round", true)) {
                sharedPaint.strokeCap = Paint.Cap.ROUND
            }
            else if (it.equals("square", true)) {
                sharedPaint.strokeCap = Paint.Cap.SQUARE
            }
        }
        shape.styles?.lineJoin?.let {
            if (it.equals("miter", true)) {
                sharedPaint.strokeJoin = Paint.Join.MITER
            }
            else if (it.equals("round", true)) {
                sharedPaint.strokeJoin = Paint.Join.ROUND
            }
            else if (it.equals("bevel", true)) {
                sharedPaint.strokeJoin = Paint.Join.BEVEL
            }
        }
        shape.styles?.miterLimit?.let {
            sharedPaint.strokeMiter = it.toFloat()
        }
        shape.styles?.lineDash?.let {
            if (it.size == 3) {
                sharedPaint.pathEffect = DashPathEffect(floatArrayOf(
                        (if (it[0] < 1.0f) 1.0f else it[0]),
                        (if (it[1] < 0.1f) 0.1f else it[1])
                ), it[2])
            }
        }
    }

}
