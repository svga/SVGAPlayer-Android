package com.opensource.svgaplayer.drawer

import android.graphics.*
import android.text.StaticLayout
import android.widget.ImageView
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGAVideoEntity
import com.opensource.svgaplayer.entities.SVGAVideoShapeEntity

/**
 * Created by cuiminghui on 2017/3/29.
 */

internal class SVGACanvasDrawer(videoItem: SVGAVideoEntity, val dynamicItem: SVGADynamicEntity) : SGVADrawer(videoItem) {

    private val sharedValues = ShareValues()
    private val drawTextCache: HashMap<String, Bitmap> = hashMapOf()
    private val pathCache = PathCache()

    override fun drawFrame(canvas: Canvas, frameIndex: Int, scaleType: ImageView.ScaleType) {
        super.drawFrame(canvas,frameIndex, scaleType)
        this.pathCache.onSizeChanged(canvas)
        val sprites = requestFrameSprites(frameIndex)
        sprites.forEach {
            drawSprite(it, canvas, frameIndex)
        }
        playAudio(frameIndex)
    }

    private fun playAudio(frameIndex: Int) {
        this.videoItem.audios.forEach { audio ->
            if (audio.startFrame == frameIndex) {
                this.videoItem.soundPool?.let { soundPool ->
                    audio.soundID?.let {soundID ->
                        audio.playID = soundPool.play(soundID, 1.0f, 1.0f, 1, 0, 1.0f)
                    }
                }
            }
            if (audio.endFrame <= frameIndex) {
                audio.playID?.let {
                    this.videoItem.soundPool?.stop(it)
                }
                audio.playID = null
            }
        }
    }

    private fun shareFrameMatrix(transform: Matrix): Matrix {
        val matrix = this.sharedValues.sharedMatrix()
        matrix.postScale(scaleInfo.scaleFx, scaleInfo.scaleFy)
        matrix.postTranslate(scaleInfo.tranFx, scaleInfo.tranFy)
        matrix.preConcat(transform)
        return matrix
    }

    private fun drawSprite(sprite: SVGADrawerSprite, canvas :Canvas, frameIndex: Int) {
        drawImage(sprite, canvas)
        drawShape(sprite, canvas)
        drawDynamic(sprite, canvas, frameIndex)
    }

    private fun drawImage(sprite: SVGADrawerSprite, canvas :Canvas) {
        val imageKey = sprite.imageKey ?: return
        val isHidden = dynamicItem.dynamicHidden[imageKey] == true
        if (isHidden) { return }
        val drawingBitmap = (dynamicItem.dynamicImage[imageKey] ?: videoItem.images[imageKey]) ?: return
        val frameMatrix = shareFrameMatrix(sprite.frameEntity.transform)
        val paint = this.sharedValues.sharedPaint()
        paint.isAntiAlias = videoItem.antiAlias
        paint.isFilterBitmap = videoItem.antiAlias
        paint.alpha = (sprite.frameEntity.alpha * 255).toInt()
        if (sprite.frameEntity.maskPath != null) {
            val maskPath = sprite.frameEntity.maskPath ?: return
            canvas.save()
            paint.reset()
            val path = this.sharedValues.sharedPath()
            maskPath.buildPath(path)
            path.transform(frameMatrix)
            canvas.clipPath(path)
            frameMatrix.preScale((sprite.frameEntity.layout.width / drawingBitmap.width).toFloat(), (sprite.frameEntity.layout.width / drawingBitmap.width).toFloat())
            canvas.drawBitmap(drawingBitmap, frameMatrix, paint)
            canvas.restore()
        }
        else {
            frameMatrix.preScale((sprite.frameEntity.layout.width / drawingBitmap.width).toFloat(), (sprite.frameEntity.layout.width / drawingBitmap.width).toFloat())
            canvas.drawBitmap(drawingBitmap, frameMatrix, paint)
        }
        drawTextOnBitmap(canvas, drawingBitmap, sprite, frameMatrix)
    }

    private fun drawTextOnBitmap(canvas: Canvas, drawingBitmap: Bitmap, sprite: SVGADrawerSprite, frameMatrix: Matrix) {
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
            val paint = this.sharedValues.sharedPaint()
            paint.isAntiAlias = videoItem.antiAlias
            if (sprite.frameEntity.maskPath != null) {
                val maskPath = sprite.frameEntity.maskPath ?: return@let
                canvas.save()
                canvas.concat(frameMatrix)
                canvas.clipRect(0, 0, drawingBitmap.width, drawingBitmap.height)
                val bitmapShader = BitmapShader(textBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                paint.shader = bitmapShader
                val path = this.sharedValues.sharedPath()
                maskPath.buildPath(path)
                canvas.drawPath(path, paint)
                canvas.restore()
            }
            else {
                paint.isFilterBitmap = videoItem.antiAlias
                canvas.drawBitmap(textBitmap, frameMatrix, paint)
            }
        }
    }

    private fun drawShape(sprite: SVGADrawerSprite, canvas: Canvas) {
        val frameMatrix = shareFrameMatrix(sprite.frameEntity.transform)
        sprite.frameEntity.shapes.forEach { shape ->
            shape.buildPath()
            shape.shapePath?.let {
                val paint = this.sharedValues.sharedPaint()
                paint.reset()
                paint.isAntiAlias = videoItem.antiAlias
                paint.alpha = (sprite.frameEntity.alpha * 255).toInt()
                val path = this.sharedValues.sharedPath()
                path.reset()
                path.addPath(this.pathCache.buildPath(shape))
                val shapeMatrix = this.sharedValues.sharedMatrix2()
                shapeMatrix.reset()
                shape.transform?.let {
                    shapeMatrix.postConcat(it)
                }
                shapeMatrix.postConcat(frameMatrix)
                path.transform(shapeMatrix)
                shape.styles?.fill?.let {
                    if (it != 0x00000000) {
                        paint.style = Paint.Style.FILL
                        paint.color = it
                        paint.alpha = Math.min(255, Math.max(0, (sprite.frameEntity.alpha * 255).toInt()))
                        if (sprite.frameEntity.maskPath !== null) canvas.save()
                        sprite.frameEntity.maskPath?.let { maskPath ->
                            val path2 = this.sharedValues.sharedPath2()
                            maskPath.buildPath(path2)
                            path2.transform(frameMatrix)
                            canvas.clipPath(path2)
                        }
                        canvas.drawPath(path, paint)
                        if (sprite.frameEntity.maskPath !== null) canvas.restore()
                    }
                }
                shape.styles?.strokeWidth?.let {
                    if (it > 0) {
                        paint.style = Paint.Style.STROKE
                        shape.styles?.stroke?.let {
                            paint.color = it
                            paint.alpha = Math.min(255, Math.max(0, (sprite.frameEntity.alpha * 255).toInt()))
                        }
                        val scale = matrixScale(frameMatrix)
                        shape.styles?.strokeWidth?.let {
                            paint.strokeWidth = it * scale
                        }
                        shape.styles?.lineCap?.let {
                            when {
                                it.equals("butt", true) -> paint.strokeCap = Paint.Cap.BUTT
                                it.equals("round", true) -> paint.strokeCap = Paint.Cap.ROUND
                                it.equals("square", true) -> paint.strokeCap = Paint.Cap.SQUARE
                            }
                        }
                        shape.styles?.lineJoin?.let {
                            when {
                                it.equals("miter", true) -> paint.strokeJoin = Paint.Join.MITER
                                it.equals("round", true) -> paint.strokeJoin = Paint.Join.ROUND
                                it.equals("bevel", true) -> paint.strokeJoin = Paint.Join.BEVEL
                            }
                        }
                        shape.styles?.miterLimit?.let {
                            paint.strokeMiter = it.toFloat() * scale
                        }
                        shape.styles?.lineDash?.let {
                            if (it.size == 3 && (it[0] > 0 || it[1] > 0)) {
                                paint.pathEffect = DashPathEffect(floatArrayOf(
                                        (if (it[0] < 1.0f) 1.0f else it[0]) * scale,
                                        (if (it[1] < 0.1f) 0.1f else it[1]) * scale
                                ), it[2] * scale)
                            }
                        }
                        if (sprite.frameEntity.maskPath !== null) canvas.save()
                        sprite.frameEntity.maskPath?.let { maskPath ->
                            val path2 = this.sharedValues.sharedPath2()
                            maskPath.buildPath(path2)
                            path2.transform(frameMatrix)
                            canvas.clipPath(path2)
                        }
                        canvas.drawPath(path, paint)
                        if (sprite.frameEntity.maskPath !== null) canvas.restore()
                    }
                }
            }

        }
    }

    private val matrixScaleTempValues = FloatArray(16)

    private fun matrixScale(matrix: Matrix): Float {
        matrix.getValues(matrixScaleTempValues)
        if (matrixScaleTempValues[0] == 0f) {
            return 0f
        }
        var A = matrixScaleTempValues[0].toDouble()
        var B = matrixScaleTempValues[3].toDouble()
        var C = matrixScaleTempValues[1].toDouble()
        var D = matrixScaleTempValues[4].toDouble()
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
        return if (scaleInfo.ratioX) Math.abs(scaleX.toFloat()) else Math.abs(scaleY.toFloat())
    }

    private fun drawDynamic(sprite: SVGADrawerSprite, canvas: Canvas, frameIndex: Int) {
        val imageKey = sprite.imageKey ?: return
        dynamicItem.dynamicDrawer[imageKey]?.let {
            val frameMatrix = shareFrameMatrix(sprite.frameEntity.transform)
            canvas.save()
            canvas.concat(frameMatrix)
            it.invoke(canvas, frameIndex)
            canvas.restore()
        }
    }

    class ShareValues {

        private val sharedPaint = Paint()
        private val sharedPath = Path()
        private val sharedPath2 = Path()
        private val sharedMatrix = Matrix()
        private val sharedMatrix2 = Matrix()

        fun sharedPaint(): Paint {
            sharedPaint.reset()
            return sharedPaint
        }

        fun sharedPath(): Path {
            sharedPath.reset()
            return sharedPath
        }

        fun sharedPath2(): Path {
            sharedPath2.reset()
            return sharedPath2
        }

        fun sharedMatrix(): Matrix {
            sharedMatrix.reset()
            return sharedMatrix
        }

        fun sharedMatrix2(): Matrix {
            sharedMatrix2.reset()
            return sharedMatrix2
        }

    }

    class PathCache {

        private var canvasWidth: Int = 0
        private var canvasHeight: Int = 0
        private val cache = HashMap<SVGAVideoShapeEntity,Path>()

        fun onSizeChanged(canvas: Canvas) {
            if(this.canvasWidth != canvas.width || this.canvasHeight != canvas.height){
                this.cache.clear()
            }
            this.canvasWidth = canvas.width
            this.canvasHeight = canvas.height
        }

        fun buildPath(shape: SVGAVideoShapeEntity): Path {
            if(!this.cache.containsKey(shape)){
                val path = Path()
                path.set(shape.shapePath)
                this.cache[shape] = path
            }
            return this.cache[shape]!!
        }

    }

}
