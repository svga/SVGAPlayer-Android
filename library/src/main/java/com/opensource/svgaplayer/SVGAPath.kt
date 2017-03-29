package com.opensource.svgaplayer

import android.graphics.Path

import java.io.Serializable
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections

internal class SVGAPath {

    val VALID_METHODS: List<String> = listOf("M", "L", "H", "V", "C", "S", "Q", "R", "A", "Z", "m", "l", "h", "v", "c", "s", "q", "r", "a", "z")

    var path: Path? = null
        private set

    constructor(strValue: String) {
        buildPath(strValue.split("[,\\s+]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray())
    }

    private fun buildPath(items: Array<String>) {
        val finalPath = Path()
        var currentMethod = ""
        val args = ArrayList<SVGAPoint>()
        var argLast: String? = null
        for (item in items) {
            if (item.length < 1) {
                continue
            }
            val firstLetter = item.substring(0, 1)
            if (VALID_METHODS.contains(firstLetter)) {
                if (null != argLast) {
                    args.add(SVGAPoint(0.0f, 0.0f, argLast.toFloat()))
                }
                this.operate(finalPath, currentMethod, args)
                args.clear()
                currentMethod = firstLetter
                argLast = item.substring(1)
            } else {
                if (null != argLast && argLast.trim { it <= ' ' }.length > 0) {
                    args.add(SVGAPoint(argLast.toFloat(), item.toFloat(), 0.0f))
                    argLast = null
                } else {
                    argLast = item
                }
            }
        }
        this.operate(finalPath, currentMethod, args)
        path = finalPath
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
