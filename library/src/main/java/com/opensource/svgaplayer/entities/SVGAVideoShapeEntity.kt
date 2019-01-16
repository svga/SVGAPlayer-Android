package com.opensource.svgaplayer.entities

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import com.opensource.svgaplayer.proto.ShapeEntity
import org.json.JSONObject
import java.util.HashMap

/**
 * Created by cuiminghui on 2017/2/22.
 */

val sharedPath = Path()

internal class SVGAVideoShapeEntity {

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

    constructor(obj: JSONObject) {
        parseType(obj)
        parseArgs(obj)
        parseStyles(obj)
        parseTransform(obj)
    }

    constructor(obj: ShapeEntity) {
        parseType(obj)
        parseArgs(obj)
        parseStyles(obj)
        parseTransform(obj)
    }

    val isKeep: Boolean
        get() = type == Type.keep

    var shapePath: Path? = null

    private fun parseType(obj: JSONObject) {
        obj.optString("type")?.let {
            when {
                it.equals("shape", ignoreCase = true) -> type = Type.shape
                it.equals("rect", ignoreCase = true) -> type = Type.rect
                it.equals("ellipse", ignoreCase = true) -> type = Type.ellipse
                it.equals("keep", ignoreCase = true) -> type = Type.keep
            }
        }
    }

    private fun parseType(obj: ShapeEntity) {
        obj.type?.let {
            type = when (it) {
                ShapeEntity.ShapeType.SHAPE -> Type.shape
                ShapeEntity.ShapeType.RECT -> Type.rect
                ShapeEntity.ShapeType.ELLIPSE -> Type.ellipse
                ShapeEntity.ShapeType.KEEP -> Type.keep
            }
        }
    }

    private fun parseArgs(obj: JSONObject) {
        val args = HashMap<String, Any>()
        obj.optJSONObject("args")?.let { values ->
            values.keys().forEach { key ->
                values.get(key)?.let {
                    args.put(key, it)
                }
            }
            this.args = args
        }
    }

    private fun parseArgs(obj: ShapeEntity) {
        val args = HashMap<String, Any>()
        obj.shape?.let {
            it.d?.let { args.put("d", it) }
        }
        obj.ellipse?.let {
            args.put("x", it.x ?: 0.0f)
            args.put("y", it.y ?: 0.0f)
            args.put("radiusX", it.radiusX ?: 0.0f)
            args.put("radiusY", it.radiusY ?: 0.0f)
        }
        obj.rect?.let {
            args.put("x", it.x ?: 0.0f)
            args.put("y", it.y ?: 0.0f)
            args.put("width", it.width ?: 0.0f)
            args.put("height", it.height ?: 0.0f)
            args.put("cornerRadius", it.cornerRadius ?: 0.0f)
        }
        this.args = args
    }

    private fun parseStyles(obj: JSONObject) {
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
                for (i in 0 until it.length()) {
                    styles.lineDash[i] = it.optDouble(i, 0.0).toFloat()
                }
            }
            this.styles = styles
        }
    }

    private fun parseStyles(obj: ShapeEntity) {
        obj.styles?.let {
            val styles = Styles()
            it.fill?.let {
                styles.fill = Color.argb(((it.a ?: 0.0f) * 255).toInt(), ((it.r ?: 0.0f) * 255).toInt(), ((it.g ?: 0.0f) * 255).toInt(), ((it.b ?: 0.0f) * 255).toInt())
            }
            it.stroke?.let {
                styles.stroke = Color.argb(((it.a ?: 0.0f) * 255).toInt(), ((it.r ?: 0.0f) * 255).toInt(), ((it.g ?: 0.0f) * 255).toInt(), ((it.b ?: 0.0f) * 255).toInt())
            }
            styles.strokeWidth = it.strokeWidth ?: 0.0f
            it.lineCap?.let {
                when (it) {
                    ShapeEntity.ShapeStyle.LineCap.LineCap_BUTT -> styles.lineCap = "butt"
                    ShapeEntity.ShapeStyle.LineCap.LineCap_ROUND -> styles.lineCap = "round"
                    ShapeEntity.ShapeStyle.LineCap.LineCap_SQUARE -> styles.lineCap = "square"
                }
            }
            it.lineJoin?.let {
                when (it) {
                    ShapeEntity.ShapeStyle.LineJoin.LineJoin_BEVEL -> styles.lineJoin = "bevel"
                    ShapeEntity.ShapeStyle.LineJoin.LineJoin_MITER -> styles.lineJoin = "miter"
                    ShapeEntity.ShapeStyle.LineJoin.LineJoin_ROUND -> styles.lineJoin = "round"
                }
            }
            styles.miterLimit = (it.miterLimit ?: 0.0f).toInt()
            styles.lineDash = kotlin.FloatArray(3)
            it.lineDashI?.let { styles.lineDash[0] = it }
            it.lineDashII?.let { styles.lineDash[1] = it }
            it.lineDashIII?.let { styles.lineDash[2] = it }
            this.styles = styles
        }
    }

    private fun parseTransform(obj: JSONObject) {
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

    private fun parseTransform(obj: ShapeEntity) {
        obj.transform?.let {
            val transform = Matrix()
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
            this.transform = transform
        }
    }


    fun buildPath() {
        if (this.shapePath != null) {
            return
        }
        sharedPath.reset()
        if (this.type == Type.shape) {
            (this.args?.get("d") as? String)?.let {
                SVGAPathEntity(it).buildPath(sharedPath)
            }
        }
        else if (this.type == Type.ellipse) {
            val xv = this.args?.get("x") as? Number ?: return
            val yv = this.args?.get("y") as? Number ?: return
            val rxv = this.args?.get("radiusX") as? Number ?: return
            val ryv = this.args?.get("radiusY") as? Number ?: return
            val x = xv.toFloat()
            val y = yv.toFloat()
            val rx = rxv.toFloat()
            val ry = ryv.toFloat()
            sharedPath.addOval(RectF(x - rx, y - ry, x + rx, y + ry), Path.Direction.CW)
        }
        else if (this.type == Type.rect) {
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
            sharedPath.addRoundRect(RectF(x, y, x + width, y + height), cornerRadius, cornerRadius, Path.Direction.CW)
        }
        this.shapePath = Path()
        this.shapePath?.set(sharedPath)
    }

}
