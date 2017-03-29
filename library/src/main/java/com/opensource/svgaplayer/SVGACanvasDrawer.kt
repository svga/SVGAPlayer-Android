package com.opensource.svgaplayer

import android.graphics.*

/**
 * Created by cuiminghui on 2017/3/29.
 */

class SVGACanvasDrawer(videoItem: SVGAVideoEntity, val canvas: Canvas) : SGVADrawer(videoItem) {

    val sharedPaint = Paint()
    val sharedContentTransform = Matrix()

    override fun drawFrame(frameIndex: Int) {
        super.drawFrame(frameIndex)
        val t = System.currentTimeMillis()
        val sprites = requestFrameSprites(frameIndex)
        sprites.forEach {
            drawSprite(it)
        }
        val e = System.currentTimeMillis()
        val o = e - t
        System.out.println("帧渲染耗时" + o + "ms")
    }

    private fun drawSprite(sprite: SVGADrawerSprite) {
        videoItem.images[sprite.imageKey]?.let {
            sharedPaint.reset()
            sharedContentTransform.reset()
            sharedPaint.alpha = (sprite.frameEntity.alpha * 255).toInt()
            sharedContentTransform.setScale((canvas.width / videoItem.videoSize.width).toFloat(), (canvas.width / videoItem.videoSize.width).toFloat())
            sharedContentTransform.setConcat(sharedContentTransform, sprite.frameEntity.transform)
            sharedContentTransform.postScale((sprite.frameEntity.layout.width / it.width).toFloat(), (sprite.frameEntity.layout.width / it.width).toFloat())
            if (sprite.frameEntity.maskPath != null) {
                val maskPath = sprite.frameEntity.maskPath ?: return@let
                canvas.save()
                canvas.concat(sharedContentTransform)
                canvas.clipRect(0, 0, it.width, it.height)
                val bitmapShader = BitmapShader(it, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                sharedPaint.shader = bitmapShader
                canvas.drawPath(maskPath, sharedPaint)
                canvas.restore()
            }
            else {
                canvas.drawBitmap(it, sharedContentTransform, sharedPaint)
            }
        }
    }

}
