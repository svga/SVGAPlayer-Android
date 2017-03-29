package com.opensource.svgaplayer

/**
 * Created by cuiminghui on 2017/3/29.
 */

open class SGVADrawer(val videoItem: SVGAVideoEntity) {

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

    open fun drawFrame(frameIndex: Int) {

    }

}
