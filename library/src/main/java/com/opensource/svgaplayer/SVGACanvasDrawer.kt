package com.opensource.svgaplayer

import android.graphics.*
import android.text.StaticLayout
import android.widget.ImageView


/**
 * Created by cuiminghui on 2017/3/29.
 */

class SVGACanvasDrawer(videoItem: SVGAVideoEntity, val dynamicItem: SVGADynamicEntity) : SGVADrawer(videoItem) {

    var canvas: Canvas? = null
    var scaleEntity:ScaleEntity = ScaleEntity()

    private val sharedPaint = Paint()
    private val sharedPath = Path()
    private val sharedPath2 = Path()
    private val sharedContentTransform = Matrix()
    private val textBitmapCache: HashMap<String, Bitmap> = hashMapOf()

    override fun drawFrame(frameIndex: Int, scaleType: ImageView.ScaleType) {
        super.drawFrame(frameIndex, scaleType)
        val sprites = requestFrameSprites(frameIndex)
        performScaleType(scaleType)
        sprites.forEach {
            drawSprite(it)
        }
    }

    private fun enableScaleEntity(){
        sharedContentTransform.reset()
        sharedContentTransform.postScale(scaleEntity.scaleFx, scaleEntity.scaleFy)
        sharedContentTransform.postTranslate(scaleEntity.tranFx, scaleEntity.tranFy)
    }

    private fun performScaleType(scaleType: ImageView.ScaleType) {
        val canvas = this.canvas ?: return
        scaleEntity.performScaleType(canvas.width.toFloat(),canvas.height.toFloat(),videoItem.videoSize.width.toFloat(),videoItem.videoSize.height.toFloat(),scaleType)
    }

    private fun drawSprite(sprite: SVGADrawerSprite) {
        drawImage(sprite)
        drawShape(sprite)
    }

    private fun drawImage(sprite: SVGADrawerSprite) {
        val canvas = this.canvas ?: return
        val imageKey = sprite.imageKey ?: return
        (dynamicItem.dynamicImage[imageKey] ?: videoItem.images[imageKey])?.let {
            sharedPaint.reset()
            sharedPaint.isAntiAlias = videoItem.antiAlias
            sharedPaint.isFilterBitmap = videoItem.antiAlias
            sharedPaint.alpha = (sprite.frameEntity.alpha * 255).toInt()
            enableScaleEntity()
            sharedContentTransform.preConcat(sprite.frameEntity.transform)
            if (sprite.frameEntity.maskPath != null) {
                val maskPath = sprite.frameEntity.maskPath ?: return@let
                canvas.save()
                sharedPath.reset()
                maskPath.buildPath(sharedPath)
                sharedPath.transform(sharedContentTransform)
                canvas.clipPath(sharedPath)
                sharedContentTransform.preScale((sprite.frameEntity.layout.width / it.width).toFloat(), (sprite.frameEntity.layout.width / it.width).toFloat())
                canvas.drawBitmap(it, sharedContentTransform, sharedPaint)
                canvas.restore()
            }
            else {
                sharedContentTransform.preScale((sprite.frameEntity.layout.width / it.width).toFloat(), (sprite.frameEntity.layout.width / it.width).toFloat())
                canvas.drawBitmap(it, sharedContentTransform, sharedPaint)
            }
            drawText(it, sprite)
        }
    }

    private fun drawText(drawingBitmap: Bitmap, sprite: SVGADrawerSprite) {
        if (dynamicItem.isTextDirty) {
            this.textBitmapCache.clear()
            dynamicItem.isTextDirty = false
        }
        val canvas = this.canvas ?: return
        val imageKey = sprite.imageKey ?: return
        var textBitmap: Bitmap? = null
        dynamicItem.dynamicText[imageKey]?.let { drawingText ->
            dynamicItem.dynamicTextPaint[imageKey]?.let { drawingTextPaint ->
                textBitmapCache[imageKey]?.let {
                    textBitmap = it
                } ?: kotlin.run {
                    textBitmap = Bitmap.createBitmap(drawingBitmap.width, drawingBitmap.height, Bitmap.Config.ARGB_8888)
                    val textCanvas = Canvas(textBitmap)
                    drawingTextPaint.isAntiAlias = true
                    val bounds = Rect()
                    drawingTextPaint.getTextBounds(drawingText, 0, drawingText.length, bounds)
                    val x = (drawingBitmap.width - bounds.width()) / 2.0
                    val targetRectTop = 0
                    val targetRectBottom = drawingBitmap.height
                    val y = (targetRectBottom + targetRectTop - drawingTextPaint.fontMetrics.bottom - drawingTextPaint.fontMetrics.top) / 2
                    textCanvas.drawText(drawingText, x.toFloat(), y, drawingTextPaint)
                    textBitmapCache.put(imageKey, textBitmap as Bitmap)
                }
            }
        }
        dynamicItem.dynamicLayoutText[imageKey]?.let {
            textBitmapCache[imageKey]?.let {
                textBitmap = it
            } ?: kotlin.run {
                var layout = StaticLayout(it.text, 0, it.text.length, it.paint, drawingBitmap.width, it.alignment, it.spacingMultiplier, it.spacingAdd, false)
                textBitmap = Bitmap.createBitmap(drawingBitmap.width, drawingBitmap.height, Bitmap.Config.ARGB_8888)
                val textCanvas = Canvas(textBitmap)
                textCanvas.translate(0f, ((drawingBitmap.height - layout.height) / 2).toFloat())
                layout.draw(textCanvas)
                textBitmapCache.put(imageKey, textBitmap as Bitmap)
            }
        }
        textBitmap?.let { textBitmap ->
            sharedPaint.reset()
            sharedPaint.isAntiAlias = videoItem.antiAlias
            if (sprite.frameEntity.maskPath != null) {
                val maskPath = sprite.frameEntity.maskPath ?: return@let
                canvas.save()
                canvas.concat(sharedContentTransform)
                canvas.clipRect(0, 0, drawingBitmap.width, drawingBitmap.height)
                val bitmapShader = BitmapShader(textBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                sharedPaint.shader = bitmapShader
                sharedPath.reset()
                maskPath.buildPath(sharedPath)
                canvas.drawPath(sharedPath, sharedPaint)
                canvas.restore()
            }
            else {
                sharedPaint.isFilterBitmap = videoItem.antiAlias
                canvas.drawBitmap(textBitmap, sharedContentTransform, sharedPaint)
            }
        }
    }

    private fun drawShape(sprite: SVGADrawerSprite) {
        val canvas = this.canvas ?: return
        enableScaleEntity()
        sharedContentTransform.preConcat(sprite.frameEntity.transform)
        sprite.frameEntity.shapes.forEach { shape ->
            sharedPath.reset()
            shape.buildPath()
            shape.shapePath?.let {
                sharedPath.addPath(it)
            }
            if (!sharedPath.isEmpty) {             
                sharedPath.transform(sharedContentTransform)
                sharedPaint.reset()
                sharedPaint.isAntiAlias = videoItem.antiAlias
                sharedPaint.alpha = (sprite.frameEntity.alpha * 255).toInt()
                shape.styles?.fill?.let {
                    if (it != 0x00000000) {
                        sharedPaint.color = it
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

    private val tValues = FloatArray(16)

    private fun requestScale(): Float {
        this.sharedContentTransform.getValues(tValues)
        if (tValues[0] == 0f) {
            return 0f
        }
        var A = tValues[0].toDouble()
        var B = tValues[3].toDouble()
        var C = tValues[1].toDouble()
        var D = tValues[4].toDouble()
        if (A * D == B * C) return 0f
        var scaleX = Math.sqrt(A * A + B * B)
        A /= scaleX
        B /= scaleX
        var skew = A * C + B * D
        C -= A * skew
        D -= B * skew
        var scaleY = Math.sqrt(C * C + D * D)
        C /= scaleY
        D /= scaleY
        skew /= scaleY
        if ( A * D < B * C ) {
            scaleX = -scaleX
        }
        return if (scaleEntity.ratioX) scaleEntity.ratio / Math.abs(scaleX.toFloat()) else scaleEntity.ratio / Math.abs(scaleY.toFloat())
    }

    private fun resetShapeStrokePaint(shape: SVGAVideoShapeEntity) {
        sharedPaint.reset()
        sharedPaint.isAntiAlias = videoItem.antiAlias
        sharedPaint.style = Paint.Style.STROKE
        shape.styles?.stroke?.let {
            sharedPaint.color = it
        }
        
        val scale = requestScale()
        shape.styles?.strokeWidth?.let {
            sharedPaint.strokeWidth = it * scale
        }
        shape.styles?.lineCap?.let {
            when {
                it.equals("butt", true) -> sharedPaint.strokeCap = Paint.Cap.BUTT
                it.equals("round", true) -> sharedPaint.strokeCap = Paint.Cap.ROUND
                it.equals("square", true) -> sharedPaint.strokeCap = Paint.Cap.SQUARE
            }
        }
        shape.styles?.lineJoin?.let {
            when {
                it.equals("miter", true) -> sharedPaint.strokeJoin = Paint.Join.MITER
                it.equals("round", true) -> sharedPaint.strokeJoin = Paint.Join.ROUND
                it.equals("bevel", true) -> sharedPaint.strokeJoin = Paint.Join.BEVEL
            }
        }
        shape.styles?.miterLimit?.let {
            sharedPaint.strokeMiter = it.toFloat() * scale
        }
        shape.styles?.lineDash?.let {
            if (it.size == 3 && it[0] > 0 && it[1] > 0) {
                sharedPaint.pathEffect = DashPathEffect(floatArrayOf(
                        (if (it[0] < 1.0f) 1.0f else it[0]) * scale,
                        (if (it[1] < 0.1f) 0.1f else it[1]) * scale
                ), it[2] * scale)
            }
        }
    }

}
