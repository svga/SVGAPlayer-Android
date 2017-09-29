package com.opensource.svgaplayer

import android.graphics.Matrix
import android.graphics.Path
import android.text.TextUtils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable

/**
 * Created by cuiminghui on 2016/10/17.
 */
class SVGAVideoSpriteFrameEntity {

    var alpha: Double
    var layout = SVGARect(0.0, 0.0, 0.0, 0.0)
    var transform = Matrix()
    var maskPath: SVGAPath? = null
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
                maskPath = SVGAPath(d)
            }
        }
        obj.optJSONArray("shapes")?.let {
            val mutableList: MutableList<SVGAVideoShapeEntity> = mutableListOf()
            for (i in 0..it.length() - 1) {
                it.optJSONObject(i)?.let {
                    mutableList.add(SVGAVideoShapeEntity(it))
                }
            }
            shapes = mutableList.toList()
        }
    }

    constructor(obj: ComOpensourceSvgaVideo.FrameEntity) {
        this.alpha = obj.alpha.toDouble()
        if (obj.hasLayout()) {
            obj.layout?.let {
                this.layout = SVGARect(it.x.toDouble(), it.y.toDouble(), it.width.toDouble(), it.height.toDouble())
            }
        }
        if (obj.hasTransform()) {
            obj.transform?.let {
                val arr = FloatArray(9)
                val a = it.a
                val b = it.b
                val c = it.c
                val d = it.d
                val tx = it.tx
                val ty = it.ty
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
        }
        obj.clipPath?.takeIf { it.isNotEmpty() }?.let {
            maskPath = SVGAPath(it)
        }
        this.shapes = obj.shapesList.map {
            return@map SVGAVideoShapeEntity(it)
        }
    }

}
