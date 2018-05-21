package com.opensource.svgaplayer

import android.graphics.Canvas
import android.graphics.Rect
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

    open fun drawFrame(canvas : Canvas, frameIndex: Int,rect: Rect, scaleType: ImageView.ScaleType) {
        performScaleType(rect,scaleType)
    }

    open fun performScaleType(rect: Rect,scaleType: ImageView.ScaleType) {
        scaleEntity.performScaleType(rect.width().toFloat(),rect.height().toFloat(),videoItem.videoSize.width.toFloat(),videoItem.videoSize.height.toFloat(),scaleType)
    }

}
