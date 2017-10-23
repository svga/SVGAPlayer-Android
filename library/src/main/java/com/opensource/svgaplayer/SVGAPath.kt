package com.opensource.svgaplayer

import android.graphics.Path
import java.util.*

val VALID_METHODS: List<String> = listOf("M", "L", "H", "V", "C", "S", "Q", "R", "A", "Z", "m", "l", "h", "v", "c", "s", "q", "r", "a", "z")

class SVGAPath(private val strValue: String) {

    private var cachedPath: Path? = null

    fun buildPath(toPath: Path) {
        cachedPath?.let {
            toPath.addPath(it)
            return
        }
        val cachedPath = Path()
        var currentMethod = ""
        val args = ArrayList<SVGAPoint>()
        var argLast: String? = null
        val items = strValue.split("[,\\s+]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        for (item in items) {
            if (item.isEmpty()) {
                continue
            }
            val firstLetter = item.substring(0, 1)
            if (VALID_METHODS.contains(firstLetter)) {
                argLast?.takeIf { it.isNotEmpty() }?.let {
                    args.add(SVGAPoint(0.0f, 0.0f, try {it.toFloat()} catch (e: Exception) { 0.0f }))
                }
                this.operate(cachedPath, currentMethod, args)
                args.clear()
                currentMethod = firstLetter
                argLast = item.substring(1)
            } else {
                argLast = if (null != argLast && argLast.trim { it <= ' ' }.isNotEmpty()) {
                    args.add(SVGAPoint(try {
                        argLast.toFloat()
                    } catch (e: Exception) {0.0f}, try {
                        item.toFloat()
                    } catch (e: Exception) {0.0f}, 0.0f))
                    null
                } else {
                    item
                }
            }
        }
        this.operate(cachedPath, currentMethod, args)
        this.cachedPath = cachedPath
        toPath.addPath(cachedPath)
    }

    private fun operate(finalPath: Path, method: String, args: List<SVGAPoint>) {
        var currentPoint = SVGAPoint(0.0f, 0.0f, 0.0f)
        if (method == "M" && args.size == 1) {
            finalPath.moveTo(args[0].x, args[0].y)
            currentPoint = SVGAPoint(args[0].x, args[0].y, 0.0f)
        } else if (method == "m" && args.size == 1) {
            finalPath.rMoveTo(args[0].x, args[0].y)
            currentPoint = SVGAPoint(currentPoint.x + args[0].x, currentPoint.y + args[0].y, 0.0f)
        }
        if (method == "L" && args.size == 1) {
            finalPath.lineTo(args[0].x, args[0].y)
        } else if (method == "l" && args.size == 1) {
            finalPath.rLineTo(args[0].x, args[0].y)
        }
        if (method == "C" && args.size == 3) {
            finalPath.cubicTo(args[0].x, args[0].y, args[1].x, args[1].y, args[2].x, args[2].y)
        } else if (method == "c" && args.size == 3) {
            finalPath.rCubicTo(args[0].x, args[0].y, args[1].x, args[1].y, args[2].x, args[2].y)
        }
        if (method == "Q" && args.size == 2) {
            finalPath.quadTo(args[0].x, args[0].y, args[1].x, args[1].y)
        } else if (method == "q" && args.size == 2) {
            finalPath.rQuadTo(args[0].x, args[0].y, args[1].x, args[1].y)
        }
        if (method == "H" && args.size == 1) {
            finalPath.lineTo(args[0].value, currentPoint.y)
        } else if (method == "h" && args.size == 1) {
            finalPath.rLineTo(args[0].value, 0f)
        }
        if (method == "V" && args.size == 1) {
            finalPath.lineTo(currentPoint.x, args[0].value)
        } else if (method == "v" && args.size == 1) {
            finalPath.rLineTo(0f, args[0].value)
        }
        if (method == "Z" && args.size == 1) {
            finalPath.close()
        } else if (method == "z" && args.size == 1) {
            finalPath.close()
        }
    }

}
