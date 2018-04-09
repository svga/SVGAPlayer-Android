package com.opensource.svgaplayer

import android.graphics.*
import android.text.StaticLayout
import android.widget.ImageView


/**
 * Created by cuiminghui on 2017/3/29.
 */

class SVGACanvasDrawer(videoItem: SVGAVideoEntity, val dynamicItem: SVGADynamicEntity) : SGVADrawer(videoItem) {

    private var canvasW = 0
    private var canvasH = 0
    private val sharedPaint = Paint()
    private val sharedPath = Path()
    private val sharedPath2 = Path()
    private val sharedShapeMatrix= Matrix()
    private val sharedFrameMatrix= Matrix()
    private val drawTextCache: HashMap<String, Bitmap> = hashMapOf()
    private val drawPathCache = HashMap<SVGAVideoShapeEntity,Path>()

    override fun drawFrame(canvas :Canvas, frameIndex: Int, scaleType: ImageView.ScaleType) {
        super.drawFrame(canvas,frameIndex, scaleType)
        resetCachePath(canvas)
        val sprites = requestFrameSprites(frameIndex)
        sprites.forEach {
            drawSprite(it, canvas, frameIndex)
        }
    }

    private fun resetCachePath(canvas :Canvas){
        if(canvasW != canvas.width || canvasH != canvas.height){
            drawPathCache.clear()
        }
        canvasW = canvas.width
        canvasH = canvas.height
    }

    private fun resetShareMatrix(transform :Matrix){
        sharedFrameMatrix.reset()
        sharedFrameMatrix.postScale(scaleEntity.scaleFx, scaleEntity.scaleFy)
        sharedFrameMatrix.postTranslate(scaleEntity.tranFx, scaleEntity.tranFy)
        sharedFrameMatrix.preConcat(transform)
    }

    private fun drawSprite(sprite: SVGADrawerSprite, canvas :Canvas, frameIndex: Int) {
        drawImage(sprite, canvas)
        drawShape(sprite, canvas)
        drawDynamic(sprite, canvas, frameIndex)
    }

    private fun drawImage(sprite: SVGADrawerSprite, canvas :Canvas) {
        val imageKey = sprite.imageKey ?: return
        dynamicItem.dynamicHidden[imageKey]?.takeIf { it }?.let { return }
        (dynamicItem.dynamicImage[imageKey] ?: videoItem.images[imageKey])?.let {
            resetShareMatrix(sprite.frameEntity.transform)
            sharedPaint.reset()
            sharedPaint.isAntiAlias = videoItem.antiAlias
            sharedPaint.isFilterBitmap = videoItem.antiAlias
            sharedPaint.alpha = (sprite.frameEntity.alpha * 255).toInt()
            if (sprite.frameEntity.maskPath != null) {
                val maskPath = sprite.frameEntity.maskPath ?: return@let
                canvas.save()
                sharedPath.reset()
                maskPath.buildPath(sharedPath)
                sharedPath.transform(sharedFrameMatrix)
                canvas.clipPath(sharedPath)
                sharedFrameMatrix.preScale((sprite.frameEntity.layout.width / it.width).toFloat(), (sprite.frameEntity.layout.width / it.width).toFloat())
                canvas.drawBitmap(it, sharedFrameMatrix, sharedPaint)
                canvas.restore()
            }
            else {
                sharedFrameMatrix.preScale((sprite.frameEntity.layout.width / it.width).toFloat(), (sprite.frameEntity.layout.width / it.width).toFloat())
                canvas.drawBitmap(it, sharedFrameMatrix, sharedPaint)
            }
            drawText(canvas,it, sprite)
        }
    }

    private fun drawText(canvas :Canvas, drawingBitmap: Bitmap, sprite: SVGADrawerSprite) {
        if (dynamicItem.isTextDirty) {
            this.drawTextCache.clear()
            dynamicItem.isTextDirty = false
        }
        val imageKey = sprite.imageKey ?: return
        var textBitmap: Bitmap? = null
        dynamicItem.dynamicText[imageKey]?.let { drawingText ->
            dynamicItem.dynamicTextPaint[imageKey]?.let { drawingTextPaint ->
                drawTextCache[imageKey]?.let {
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
                    drawTextCache.put(imageKey, textBitmap as Bitmap)
                }
            }
        }
        dynamicItem.dynamicLayoutText[imageKey]?.let {
            drawTextCache[imageKey]?.let {
                textBitmap = it
            } ?: kotlin.run {
                it.paint.isAntiAlias = true
                var layout = StaticLayout(it.text, 0, it.text.length, it.paint, drawingBitmap.width, it.alignment, it.spacingMultiplier, it.spacingAdd, false)
                textBitmap = Bitmap.createBitmap(drawingBitmap.width, drawingBitmap.height, Bitmap.Config.ARGB_8888)
                val textCanvas = Canvas(textBitmap)
                textCanvas.translate(0f, ((drawingBitmap.height - layout.height) / 2).toFloat())
                layout.draw(textCanvas)
                drawTextCache.put(imageKey, textBitmap as Bitmap)
            }
        }
        textBitmap?.let { textBitmap ->
            sharedPaint.reset()
            sharedPaint.isAntiAlias = videoItem.antiAlias
            if (sprite.frameEntity.maskPath != null) {
                val maskPath = sprite.frameEntity.maskPath ?: return@let
                canvas.save()
                canvas.concat(sharedFrameMatrix)
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
                canvas.drawBitmap(textBitmap, sharedFrameMatrix, sharedPaint)
            }
        }
    }

    private fun drawShape(sprite: SVGADrawerSprite, canvas :Canvas) {
        resetShareMatrix(sprite.frameEntity.transform)
        sprite.frameEntity.shapes.forEach { shape ->
            shape.buildPath()
            shape.shapePath?.let {
                sharedPaint.reset()
                sharedPaint.isAntiAlias = videoItem.antiAlias
                sharedPaint.alpha = (sprite.frameEntity.alpha * 255).toInt()
                if(!drawPathCache.containsKey(shape)){
                    val path = Path()
                    path.set(shape.shapePath)
                    drawPathCache.put(shape,path)
                }
                sharedPath.reset()
                sharedPath.addPath(Path(drawPathCache[shape]))
                sharedShapeMatrix.reset()
                shape.transform?.let {
                    sharedShapeMatrix.postConcat(it)
                }
                sharedShapeMatrix.postConcat(sharedFrameMatrix)
                sharedPath.transform(sharedShapeMatrix)
                shape.styles?.fill?.let {
                    if (it != 0x00000000) {
                        sharedPaint.color = it
                        if (sprite.frameEntity.maskPath !== null) canvas.save()
                        sprite.frameEntity.maskPath?.let { maskPath ->
                            sharedPath2.reset()
                            maskPath.buildPath(sharedPath2)
                            sharedPath2.transform(this.sharedFrameMatrix)
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
                            sharedPath2.transform(this.sharedFrameMatrix)
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
        this.sharedFrameMatrix.getValues(tValues)
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
            if (it.size == 3 && (it[0] > 0 || it[1] > 0)) {
                sharedPaint.pathEffect = DashPathEffect(floatArrayOf(
                        (if (it[0] < 1.0f) 1.0f else it[0]) * scale,
                        (if (it[1] < 0.1f) 0.1f else it[1]) * scale
                ), it[2] * scale)
            }
        }
    }

    private fun drawDynamic(sprite: SVGADrawerSprite, canvas: Canvas, frameIndex: Int) {
        val imageKey = sprite.imageKey ?: return
        dynamicItem.dynamicDrawer[imageKey]?.let {
            resetShareMatrix(sprite.frameEntity.transform)
            canvas.save()
            canvas.concat(sharedFrameMatrix)
            it.invoke(canvas, frameIndex)
            canvas.restore()
        }
    }

}
