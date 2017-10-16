package com.opensource.svgaplayer

import android.graphics.*
import android.os.Build
import android.R.attr.x
import android.widget.ImageView


/**
 * Created by cuiminghui on 2017/3/29.
 */

class SVGACanvasDrawer(videoItem: SVGAVideoEntity, val dynamicItem: SVGADynamicEntity, val canvas: Canvas) : SGVADrawer(videoItem) {

    val sharedPaint = Paint()
    val sharedPath = Path()
    val sharedPath2 = Path()
    val sharedContentTransform = Matrix()

    override fun drawFrame(frameIndex: Int, scaleType: ImageView.ScaleType) {
        super.drawFrame(frameIndex, scaleType)
        val sprites = requestFrameSprites(frameIndex)
        sprites.forEach {
            drawSprite(it, scaleType)
        }
    }

    private fun performScaleType(scaleType: ImageView.ScaleType) {
        if (canvas.width == 0 || canvas.height == 0 || videoItem.videoSize.width == 0.0 || videoItem.videoSize.height == 0.0) {
            return
        }
        when (scaleType) {
            ImageView.ScaleType.CENTER -> {
                sharedContentTransform.postTranslate(((canvas.width - videoItem.videoSize.width) / 2.0).toFloat(), ((canvas.height - videoItem.videoSize.height) / 2.0).toFloat())
            }
            ImageView.ScaleType.CENTER_CROP -> {
                val videoRatio = (videoItem.videoSize.width / videoItem.videoSize.height)
                val canvasRatio = canvas.width.toFloat() / canvas.height.toFloat()
                if (videoRatio > canvasRatio) {
                    sharedContentTransform.postScale((canvas.height / videoItem.videoSize.height).toFloat(), (canvas.height / videoItem.videoSize.height).toFloat())
                    sharedContentTransform.postTranslate(((canvas.width - videoItem.videoSize.width * (canvas.height / videoItem.videoSize.height)) / 2.0).toFloat(), 0.0f)
                }
                else {
                    sharedContentTransform.postScale((canvas.width / videoItem.videoSize.width).toFloat(), (canvas.width / videoItem.videoSize.width).toFloat())
                    sharedContentTransform.postTranslate(0.0f, ((canvas.height - videoItem.videoSize.height * (canvas.width / videoItem.videoSize.width)) / 2.0).toFloat())
                }
            }
            ImageView.ScaleType.CENTER_INSIDE -> {
                if (videoItem.videoSize.width < canvas.width && videoItem.videoSize.height < canvas.height) {
                    sharedContentTransform.postTranslate(((canvas.width - videoItem.videoSize.width) / 2.0).toFloat(), ((canvas.height - videoItem.videoSize.height) / 2.0).toFloat())
                }
                else {
                    val videoRatio = (videoItem.videoSize.width / videoItem.videoSize.height)
                    val canvasRatio = canvas.width.toFloat() / canvas.height.toFloat()
                    if (videoRatio > canvasRatio) {
                        sharedContentTransform.postScale((canvas.width / videoItem.videoSize.width).toFloat(), (canvas.width / videoItem.videoSize.width).toFloat())
                        sharedContentTransform.postTranslate(0.0f, ((canvas.height - videoItem.videoSize.height * (canvas.width / videoItem.videoSize.width)) / 2.0).toFloat())
                    }
                    else {
                        sharedContentTransform.postScale((canvas.height / videoItem.videoSize.height).toFloat(), (canvas.height / videoItem.videoSize.height).toFloat())
                        sharedContentTransform.postTranslate(((canvas.width - videoItem.videoSize.width * (canvas.height / videoItem.videoSize.height)) / 2.0).toFloat(), 0.0f)
                    }
                }
            }
            ImageView.ScaleType.FIT_CENTER -> {
                val videoRatio = (videoItem.videoSize.width / videoItem.videoSize.height)
                val canvasRatio = canvas.width.toFloat() / canvas.height.toFloat()
                if (videoRatio > canvasRatio) {
                    sharedContentTransform.postScale((canvas.width / videoItem.videoSize.width).toFloat(), (canvas.width / videoItem.videoSize.width).toFloat())
                    sharedContentTransform.postTranslate(0.0f, ((canvas.height - videoItem.videoSize.height * (canvas.width / videoItem.videoSize.width)) / 2.0).toFloat())
                }
                else {
                    sharedContentTransform.postScale((canvas.height / videoItem.videoSize.height).toFloat(), (canvas.height / videoItem.videoSize.height).toFloat())
                    sharedContentTransform.postTranslate(((canvas.width - videoItem.videoSize.width * (canvas.height / videoItem.videoSize.height)) / 2.0).toFloat(), 0.0f)
                }
            }
            ImageView.ScaleType.FIT_START -> {
                val videoRatio = (videoItem.videoSize.width / videoItem.videoSize.height)
                val canvasRatio = canvas.width.toFloat() / canvas.height.toFloat()
                if (videoRatio > canvasRatio) {
                    sharedContentTransform.postScale((canvas.width / videoItem.videoSize.width).toFloat(), (canvas.width / videoItem.videoSize.width).toFloat())
                }
                else {
                    sharedContentTransform.postScale((canvas.height / videoItem.videoSize.height).toFloat(), (canvas.height / videoItem.videoSize.height).toFloat())
                }
            }
            ImageView.ScaleType.FIT_END -> {
                val videoRatio = (videoItem.videoSize.width / videoItem.videoSize.height)
                val canvasRatio = canvas.width.toFloat() / canvas.height.toFloat()
                if (videoRatio > canvasRatio) {
                    sharedContentTransform.postScale((canvas.width / videoItem.videoSize.width).toFloat(), (canvas.width / videoItem.videoSize.width).toFloat())
                    sharedContentTransform.postTranslate(0.0f, (canvas.height - videoItem.videoSize.height * (canvas.width / videoItem.videoSize.width)).toFloat())
                }
                else {
                    sharedContentTransform.postScale((canvas.height / videoItem.videoSize.height).toFloat(), (canvas.height / videoItem.videoSize.height).toFloat())
                    sharedContentTransform.postTranslate((canvas.width - videoItem.videoSize.width * (canvas.height / videoItem.videoSize.height)).toFloat(), 0.0f)
                }
            }
            ImageView.ScaleType.FIT_XY -> {
                sharedContentTransform.postScale((canvas.width / videoItem.videoSize.width).toFloat(), (canvas.height / videoItem.videoSize.height).toFloat())
            }
            else -> {
                sharedContentTransform.postScale((canvas.width / videoItem.videoSize.width).toFloat(), (canvas.width / videoItem.videoSize.width).toFloat())
            }
        }
    }

    private fun drawSprite(sprite: SVGADrawerSprite, scaleType: ImageView.ScaleType) {
        drawImage(sprite, scaleType)
        drawShape(sprite, scaleType)
    }

    private fun drawImage(sprite: SVGADrawerSprite, scaleType: ImageView.ScaleType) {
        (dynamicItem.dynamicImage[sprite.imageKey] ?: videoItem.images[sprite.imageKey])?.let {
            val drawingBitmap = it
            sharedPaint.reset()
            sharedContentTransform.reset()
            sharedPaint.isAntiAlias = videoItem.antiAlias
            sharedPaint.alpha = (sprite.frameEntity.alpha * 255).toInt()
            performScaleType(scaleType)
            sharedContentTransform.preConcat(sprite.frameEntity.transform)
            sharedContentTransform.preScale((sprite.frameEntity.layout.width / drawingBitmap.width).toFloat(), (sprite.frameEntity.layout.width / drawingBitmap.width).toFloat())
            if (sprite.frameEntity.maskPath != null) {
                val maskPath = sprite.frameEntity.maskPath ?: return@let
                canvas.save()
                canvas.concat(sharedContentTransform)
                canvas.clipRect(0, 0, drawingBitmap.width, drawingBitmap.height)
                val bitmapShader = BitmapShader(drawingBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                sharedPaint.shader = bitmapShader
                sharedPaint.isAntiAlias = true
                sharedPath.reset()
                maskPath.buildPath(sharedPath)
                canvas.drawPath(sharedPath, sharedPaint)
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
                    sharedPaint.isAntiAlias = true
                    sharedPath.reset()
                    maskPath.buildPath(sharedPath)
                    canvas.drawPath(sharedPath, sharedPaint)
                    canvas.restore()
                }
                else {
                    canvas.drawBitmap(textBitmap, sharedContentTransform, sharedPaint)
                }
            }
        }
    }

    private fun drawShape(sprite: SVGADrawerSprite, scaleType: ImageView.ScaleType) {
        sharedContentTransform.reset()
        performScaleType(scaleType)
        sharedContentTransform.preConcat(sprite.frameEntity.transform)
        sprite.frameEntity.shapes.forEach { shape ->
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
                        if (sprite.frameEntity.maskPath !== null) canvas.save()
                        sprite.frameEntity.maskPath?.let { maskPath ->
                            sharedPath2.reset()
                            maskPath.buildPath(sharedPath2)
                            sharedPath2.transform(this.sharedContentTransform)
                            canvas.clipPath(sharedPath2)
                        }
                        canvas.drawPath(sharedPath, sharedPaint)
                        if (sprite.frameEntity.maskPath !== null) canvas.restore()
                    }
                }
                shape.styles?.strokeWidth?.let {
                    if (it > 0) {
                        sharedPaint.reset()
                        sharedPaint.alpha = (sprite.frameEntity.alpha * 255).toInt()
                        resetShapeStrokePaint(shape)
                        if (sprite.frameEntity.maskPath !== null) canvas.save()
                        sprite.frameEntity.maskPath?.let { maskPath ->
                            sharedPath2.reset()
                            maskPath.buildPath(sharedPath2)
                            sharedPath2.transform(this.sharedContentTransform)
                            canvas.clipPath(sharedPath2)
                        }
                        canvas.drawPath(sharedPath, sharedPaint)
                        if (sprite.frameEntity.maskPath !== null) canvas.restore()
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
