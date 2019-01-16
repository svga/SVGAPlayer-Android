package com.opensource.svgaplayer.entities

import android.graphics.Matrix
import com.opensource.svgaplayer.proto.FrameEntity
import com.opensource.svgaplayer.utils.SVGARect

import org.json.JSONObject

/**
 * Created by cuiminghui on 2016/10/17.
 */
internal class SVGAVideoSpriteFrameEntity {

    var alpha: Double
    var layout = SVGARect(0.0, 0.0, 0.0, 0.0)
    var transform = Matrix()
    var maskPath: SVGAPathEntity? = null
    var shapes: List<SVGAVideoShapeEntity> = listOf()

    constructor(obj: JSONObject) {
        this.alpha = obj.optDouble("alpha", 0.0)
        obj.optJSONObject("layout")?.let {
            layout = SVGARect(it.optDouble("x", 0.0), it.optDouble("y", 0.0), it.optDouble("width", 0.0), it.optDouble("height", 0.0))
        }
        obj.optJSONObject("transform")?.let {
            val arr = FloatArray(9)
            val a = it.optDouble("a", 1.0)
            val b = it.optDouble("b", 0.0)
            val c = it.optDouble("c", 0.0)
            val d = it.optDouble("d", 1.0)
            val tx = it.optDouble("tx", 0.0)
            val ty = it.optDouble("ty", 0.0)
            arr[0] = a.toFloat()
            arr[1] = c.toFloat()
            arr[2] = tx.toFloat()
            arr[3] = b.toFloat()
            arr[4] = d.toFloat()
            arr[5] = ty.toFloat()
            arr[6] = 0.0.toFloat()
            arr[7] = 0.0.toFloat()
            arr[8] = 1.0.toFloat()
            transform.setValues(arr)
        }
        obj.optString("clipPath")?.let { d ->
            if (d.isNotEmpty()) {
                maskPath = SVGAPathEntity(d)
            }
        }
        obj.optJSONArray("shapes")?.let {
            val mutableList: MutableList<SVGAVideoShapeEntity> = mutableListOf()
            for (i in 0 until it.length()) {
                it.optJSONObject(i)?.let {
                    mutableList.add(SVGAVideoShapeEntity(it))
                }
            }
            shapes = mutableList.toList()
        }
    }

    constructor(obj: FrameEntity) {
        this.alpha = (obj.alpha ?: 0.0f).toDouble()
        obj.layout?.let {
            this.layout = SVGARect((it.x ?: 0.0f).toDouble(), (it.y
                    ?: 0.0f).toDouble(), (it.width ?: 0.0f).toDouble(), (it.height
                    ?: 0.0f).toDouble())
        }
        obj.transform?.let {
            val arr = FloatArray(9)
            val a = it.a ?: 1.0f
            val b = it.b ?: 0.0f
            val c = it.c ?: 0.0f
            val d = it.d ?: 1.0f
            val tx = it.tx ?: 0.0f
            val ty = it.ty ?: 0.0f
            arr[0] = a
            arr[1] = c
            arr[2] = tx
            arr[3] = b
            arr[4] = d
            arr[5] = ty
            arr[6] = 0.0f
            arr[7] = 0.0f
            arr[8] = 1.0f
            transform.setValues(arr)
        }
        obj.clipPath?.takeIf { it.isNotEmpty() }?.let {
            maskPath = SVGAPathEntity(it)
        }
        this.shapes = obj.shapes.map {
            return@map SVGAVideoShapeEntity(it)
        }
    }

}
