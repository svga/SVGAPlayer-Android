package com.opensource.svgaplayer

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Path
import android.os.Build

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.HashMap

/**
 * Created by cuiminghui on 2017/2/22.
 */

class SVGAVideoShapeEntity(obj: JSONObject) {

    enum class Type {
        shape,
        rect,
        ellipse,
        keep
    }

    class Styles {

        var fill = 0x00000000
            internal set

        var stroke = 0x00000000
            internal set

        var strokeWidth = 0.0f
            internal set

        var lineCap = "butt"
            internal set

        var lineJoin = "miter"
            internal set

        var miterLimit = 0
            internal set

        var lineDash = FloatArray(0)
            internal set

    }

    var type = Type.shape
        private set

    var args: Map<String, Any>? = null
        private set

    var styles: Styles? = null
        private set

    var transform: Matrix? = null
        private set

    init {
        parseType(obj)
        parseArgs(obj)
        parseStyles(obj)
        parseTransform(obj)
        buildPath()
    }

    val isKeep: Boolean
        get() = type == Type.keep

    var shapePath: Path? = null

    fun parseType(obj: JSONObject) {
        obj.optString("type")?.let {
            if (it.equals("shape", ignoreCase = true)) {
                type = Type.shape
            } else if (it.equals("rect", ignoreCase = true)) {
                type = Type.rect
            } else if (it.equals("ellipse", ignoreCase = true)) {
                type = Type.ellipse
            } else if (it.equals("keep", ignoreCase = true)) {
                type = Type.keep
            }
        }
    }

    fun parseArgs(obj: JSONObject) {
        val args = HashMap<String, Any>()
        obj.optJSONObject("args")?.let {
            val values = it
            it.keys().forEach {
                val key = it
                values.get(it)?.let {
                    args.put(key, it)
                }
            }
            this.args = args
        }
    }

    fun parseStyles(obj: JSONObject) {
        obj.optJSONObject("styles")?.let {
            val styles = Styles()
            it.optJSONArray("fill")?.let {
                if (it.length() == 4) {
                    styles.fill = Color.argb((it.optDouble(3) * 255).toInt(), (it.optDouble(0) * 255).toInt(), (it.optDouble(1) * 255).toInt(), (it.optDouble(2) * 255).toInt())
                }
            }
            it.optJSONArray("stroke")?.let {
                if (it.length() == 4) {
                    styles.stroke = Color.argb((it.optDouble(3) * 255).toInt(), (it.optDouble(0) * 255).toInt(), (it.optDouble(1) * 255).toInt(), (it.optDouble(2) * 255).toInt())
                }
            }
            styles.strokeWidth = it.optDouble("strokeWidth", 0.0).toFloat()
            styles.lineCap = it.optString("lineCap", "butt")
            styles.lineJoin = it.optString("lineJoin", "miter")
            styles.miterLimit = it.optInt("miterLimit", 0)
            it.optJSONArray("lineDash")?.let {
                styles.lineDash = FloatArray(it.length())
                for (i in 0..it.length() - 1) {
                    styles.lineDash[i] = it.optDouble(i, 0.0).toFloat()
                }
            }
            this.styles = styles
        }
    }

    fun parseTransform(obj: JSONObject) {
        obj.optJSONObject("transform")?.let {
            val transform = Matrix()
            val arr = FloatArray(9)
            val a = it.optDouble("a", 1.0)
            val b = it.optDouble("b", 0.0)
            val c = it.optDouble("c", 0.0)
            val d = it.optDouble("d", 1.0)
            val tx = it.optDouble("tx", 0.0)
            val ty = it.optDouble("ty", 0.0)
            arr[0] = a.toFloat() // a
            arr[1] = c.toFloat() // c
            arr[2] = tx.toFloat() // tx
            arr[3] = b.toFloat() // b
            arr[4] = d.toFloat() // d
            arr[5] = ty.toFloat() // ty
            arr[6] = 0.0.toFloat()
            arr[7] = 0.0.toFloat()
            arr[8] = 1.0.toFloat()
            transform.setValues(arr)
            this.transform = transform
        }
    }

    fun buildPath() {
        val aPath = Path()
        if (this.type == SVGAVideoShapeEntity.Type.shape) {
            (this.args?.get("d") as? String)?.let {
                SVGAPath(it, aPath)
            }
        }
        else if (this.type == SVGAVideoShapeEntity.Type.ellipse) {
            val xv = this.args?.get("x") as? Number ?: return
            val yv = this.args?.get("y") as? Number ?: return
            val rxv = this.args?.get("radiusX") as? Number ?: return
            val ryv = this.args?.get("radiusY") as? Number ?: return
            val x = xv.toFloat()
            val y = yv.toFloat()
            val rx = rxv.toFloat()
            val ry = ryv.toFloat()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                aPath.addOval(x - rx, y - ry, x + rx, y + ry, Path.Direction.CW)
            }
            else if (Math.abs(rx - ry) < 0.1) {
                aPath.addCircle(x, y, rx, Path.Direction.CW)
            }
        }
        else if (this.type == SVGAVideoShapeEntity.Type.rect) {
            val xv = this.args?.get("x") as? Number ?: return
            val yv = this.args?.get("y") as? Number ?: return
            val wv = this.args?.get("width") as? Number ?: return
            val hv = this.args?.get("height") as? Number ?: return
            val crv = this.args?.get("cornerRadius") as? Number ?: return
            val x = xv.toFloat()
            val y = yv.toFloat()
            val width = wv.toFloat()
            val height = hv.toFloat()
            val cornerRadius = crv.toFloat()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                aPath.addRoundRect(x, y, x + width, y + height, cornerRadius, cornerRadius, Path.Direction.CW)
            }
            else {
                aPath.addRect(x, y, x + width, y + height, Path.Direction.CW)
            }
        }
        this.shapePath = aPath
    }

}
