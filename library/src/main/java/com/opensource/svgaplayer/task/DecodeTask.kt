package com.opensource.svgaplayer.task

import com.opensource.svgaplayer.SVGAParser

abstract class DecodeTask(
        val url: String,
        val callback: SVGAParser.ParseCompletion?
) : Runnable {
    var taskCacheKey: String = ""
}