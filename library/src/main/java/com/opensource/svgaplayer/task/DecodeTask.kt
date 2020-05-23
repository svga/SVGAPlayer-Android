package com.opensource.svgaplayer.task

import com.opensource.svgaplayer.SVGAParser

/**
 * 解析任务
 */
abstract class DecodeTask(
        val url: String,
        val callback: SVGAParser.ParseCompletion?
) : Runnable {
    var taskCacheKey: String = ""
}