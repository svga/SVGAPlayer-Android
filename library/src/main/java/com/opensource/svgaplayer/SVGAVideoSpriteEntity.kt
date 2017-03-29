package com.opensource.svgaplayer

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable
import java.util.ArrayList

/**
 * Created by cuiminghui on 2016/10/17.
 */
class SVGAVideoSpriteEntity(obj: JSONObject) {

    val imageKey: String? = obj.getString("imageKey")

    val frames: List<SVGAVideoSpriteFrameEntity>

    init {
        val mutableFrames: MutableList<SVGAVideoSpriteFrameEntity> = mutableListOf()
        obj.getJSONArray("frames")?.let {
            for (i in 0..it.length() - 1) {
                val frameItem = SVGAVideoSpriteFrameEntity(it.getJSONObject(i))
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
        frames = mutableFrames.toList()
    }

}
