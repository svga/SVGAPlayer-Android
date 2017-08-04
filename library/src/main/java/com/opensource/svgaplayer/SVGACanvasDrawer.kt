package com.opensource.svgaplayer

import android.graphics.*
import android.os.Build
import android.R.attr.x



/**
 * Created by cuiminghui on 2017/3/29.
 */

class SVGACanvasDrawer(videoItem: SVGAVideoEntity, val dynamicItem: SVGADynamicEntity, val canvas: Canvas) : SGVADrawer(videoItem) {

    val sharedPaint = Paint()
    val sharedPath = Path()
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
            sharedPaint.isAntiAlias = videoItem.antiAlias
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
            sharedPath.reset()
            shape.shapePath?.let {
                sharedPath.addPath(it)
            }
            if (!sharedPath.isEmpty) {
                val thisTransform = Matrix()
                shape.transform?.let {
                    thisTransform.postConcat(it)
                }
                thisTransform.postConcat(sharedContentTransform)
                sharedPath.transform(thisTransform)
                shape.styles?.fill?.let {
                    if (it != 0x00000000) {
                        sharedPaint.reset()
                        sharedPaint.color = it
                        sharedPaint.alpha = (sprite.frameEntity.alpha * 255).toInt()
                        sharedPaint.isAntiAlias = true
                        canvas.drawPath(sharedPath, sharedPaint)
                    }
                }
                shape.styles?.strokeWidth?.let {
                    if (it > 0) {
                        sharedPaint.reset()
                        sharedPaint.alpha = (sprite.frameEntity.alpha * 255).toInt()
                        resetShapeStrokePaint(shape)
                        canvas.drawPath(sharedPath, sharedPaint)
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
