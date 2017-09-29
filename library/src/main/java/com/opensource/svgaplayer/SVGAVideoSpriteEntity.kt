package com.opensource.svgaplayer

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable
import java.util.ArrayList

/**
 * Created by cuiminghui on 2016/10/17.
 */
class SVGAVideoSpriteEntity {

    val imageKey: String?

    val frames: List<SVGAVideoSpriteFrameEntity>

    constructor(obj: JSONObject) {
        this.imageKey = obj.optString("imageKey")
        val mutableFrames: MutableList<SVGAVideoSpriteFrameEntity> = mutableListOf()
        obj.optJSONArray("frames")?.let {
            for (i in 0..it.length() - 1) {
                it.optJSONObject(i)?.let {
                    val frameItem = SVGAVideoSpriteFrameEntity(it)
                    if (frameItem.shapes.size > 0) {
                        frameItem.shapes.first()?.let {
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

    constructor(obj: ComOpensourceSvgaVideo.SpriteEntity) {
        this.imageKey = obj.imageKey
        var lastFrame: SVGAVideoSpriteFrameEntity? = null
        frames = obj.framesList.map {
            val frameItem = SVGAVideoSpriteFrameEntity(it)
            if (frameItem.shapes.size > 0) {
                frameItem.shapes.first()?.let {
                    if (it.isKeep) {
                        lastFrame?.let {
                            frameItem.shapes = it.shapes
                        }
                    }
                }
            }
            lastFrame = frameItem
            return@map frameItem
        }

    }

}
