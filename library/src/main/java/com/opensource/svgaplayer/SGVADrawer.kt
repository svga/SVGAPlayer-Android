package com.opensource.svgaplayer

import android.graphics.Canvas
import android.widget.ImageView

/**
 * Created by cuiminghui on 2017/3/29.
 */

open class SGVADrawer(val videoItem: SVGAVideoEntity) {

    val scaleEntity = ScaleEntity()

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
        performScaleType(canvas,scaleType)
    }

    open fun performScaleType(canvas : Canvas,scaleType: ImageView.ScaleType) {
        scaleEntity.performScaleType(canvas.width.toFloat(),canvas.height.toFloat(),videoItem.videoSize.width.toFloat(),videoItem.videoSize.height.toFloat(),scaleType)
    }

}
