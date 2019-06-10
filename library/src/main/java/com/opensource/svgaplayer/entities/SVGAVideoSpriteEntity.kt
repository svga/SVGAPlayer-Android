package com.opensource.svgaplayer.entities

import com.opensource.svgaplayer.proto.SpriteEntity
import org.json.JSONObject

/**
 * Created by cuiminghui on 2016/10/17.
 */
internal class SVGAVideoSpriteEntity {

    val imageKey: String?

    val frames: List<SVGAVideoSpriteFrameEntity>

    constructor(obj: JSONObject) {
        this.imageKey = obj.optString("imageKey")
        val mutableFrames: MutableList<SVGAVideoSpriteFrameEntity> = mutableListOf()
        obj.optJSONArray("frames")?.let {
            for (i in 0 until it.length()) {
                it.optJSONObject(i)?.let {
                    val frameItem = SVGAVideoSpriteFrameEntity(it)
                    if (frameItem.shapes.isNotEmpty()) {
                        frameItem.shapes.first().let {
                            if (it.isKeep && mutableFrames.size > 0) {
                                frameItem.shapes = mutableFrames.last().shapes
                            }
                        }
                    }
                    mutableFrames.add(frameItem)
                }
            }
        }
        frames = mutableFrames.toList()
    }

    constructor(obj: SpriteEntity) {
        this.imageKey = obj.imageKey
        var lastFrame: SVGAVideoSpriteFrameEntity? = null
        frames = obj.frames?.map {
            val frameItem = SVGAVideoSpriteFrameEntity(it)
            if (frameItem.shapes.isNotEmpty()) {
                frameItem.shapes.first().let {
                    if (it.isKeep) {
                        lastFrame?.let {
                            frameItem.shapes = it.shapes
                        }
                    }
                }
            }
            lastFrame = frameItem
            return@map frameItem
        } ?: listOf()

    }

}
