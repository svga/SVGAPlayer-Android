package com.opensource.svgaplayer.entities

import android.graphics.Path
import com.opensource.svgaplayer.utils.SVGAPoint
import java.util.*

private val VALID_METHODS: Set<String> = setOf("M", "L", "H", "V", "C", "S", "Q", "R", "A", "Z", "m", "l", "h", "v", "c", "s", "q", "r", "a", "z")

class SVGAPathEntity(originValue: String) {

    private val replacedValue: String = if (originValue.contains(",")) originValue.replace(",", " ") else originValue

    private var cachedPath: Path? = null

    fun buildPath(toPath: Path) {
        cachedPath?.let {
            toPath.set(it)
            return
        }
        val cachedPath = Path()
        val segments = StringTokenizer(this.replacedValue, "MLHVCSQRAZmlhvcsqraz", true)
        var currentMethod = ""
        while (segments.hasMoreTokens()) {
            val segment = segments.nextToken()
            if (segment.isEmpty()) { continue }
            if (VALID_METHODS.contains(segment)) {
                currentMethod = segment
                if (currentMethod == "Z" || currentMethod == "z") { operate(cachedPath, currentMethod, StringTokenizer("", "")) }
            }
            else {
                operate(cachedPath, currentMethod, StringTokenizer(segment, " "))
            }
        }
        this.cachedPath = cachedPath
        toPath.set(cachedPath)
    }

    private fun operate(finalPath: Path, method: String, args: StringTokenizer) {
        var x0 = 0.0f
        var y0 = 0.0f
        var x1 = 0.0f
        var y1 = 0.0f
        var x2 = 0.0f
        var y2 = 0.0f
        try {
            var index = 0
            while (args.hasMoreTokens()) {
                val s = args.nextToken()
                if (s.isEmpty()) {continue}
                if (index == 0) { x0 = s.toFloat() }
                if (index == 1) { y0 = s.toFloat() }
                if (index == 2) { x1 = s.toFloat() }
                if (index == 3) { y1 = s.toFloat() }
                if (index == 4) { x2 = s.toFloat() }
                if (index == 5) { y2 = s.toFloat() }
                index++
            }
        } catch (e: Exception) {}
        var currentPoint = SVGAPoint(0.0f, 0.0f, 0.0f)
        if (method == "M") {
            finalPath.moveTo(x0, y0)
            currentPoint = SVGAPoint(x0, y0, 0.0f)
        } else if (method == "m") {
            finalPath.rMoveTo(x0, y0)
            currentPoint = SVGAPoint(currentPoint.x + x0, currentPoint.y + y0, 0.0f)
        }
        if (method == "L") {
            finalPath.lineTo(x0, y0)
        } else if (method == "l") {
            finalPath.rLineTo(x0, y0)
        }
        if (method == "C") {
            finalPath.cubicTo(x0, y0, x1, y1, x2, y2)
        } else if (method == "c") {
            finalPath.rCubicTo(x0, y0, x1, y1, x2, y2)
        }
        if (method == "Q") {
            finalPath.quadTo(x0, y0, x1, y1)
        } else if (method == "q") {
            finalPath.rQuadTo(x0, y0, x1, y1)
        }
        if (method == "H") {
            finalPath.lineTo(x0, currentPoint.y)
        } else if (method == "h") {
            finalPath.rLineTo(x0, 0f)
        }
        if (method == "V") {
            finalPath.lineTo(currentPoint.x, x0)
        } else if (method == "v") {
            finalPath.rLineTo(0f, x0)
        }
        if (method == "Z") {
            finalPath.close()
        }
        else if (method == "z") {
            finalPath.close()
        }
    }

}
