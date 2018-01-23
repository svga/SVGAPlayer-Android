package com.opensource.svgaplayer

import android.graphics.Canvas
import android.widget.ImageView

/**
 * Created by cuiminghui on 2017/3/29.
 */

open class SGVADrawer(val videoItem: SVGAVideoEntity) {

    var canvas: Canvas? = null
    var canvasSizeChange = false

    inner class SVGADrawerSprite(val imageKey: String?, val frameEntity: SVGAVideoSpriteFrameEntity)

    internal fun requestFrameSprites(frameIndex: Int): List<SVGADrawerSprite> {
        return videoItem.sprites.mapNotNull {
            if (frameIndex < it.frames.size) {
                if (it.frames[frameIndex].alpha <= 0.0) {
                    return@mapNotNull null
                }
                return@mapNotNull SVGADrawerSprite(it.imageKey, it.frames[frameIndex])
            }
            return@mapNotNull null
        }
    }

    open fun drawFrame(canvas : Canvas, frameIndex: Int, scaleType: ImageView.ScaleType) {
        canvasSizeChange = true
        this.canvas?.let {
            canvasSizeChange = !(it.width == canvas.width && it.height == canvas.height)
        }
        this.canvas = canvas
    }

}
