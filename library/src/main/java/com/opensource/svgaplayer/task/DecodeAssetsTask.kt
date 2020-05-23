package com.opensource.svgaplayer.task

import com.opensource.svgaplayer.SVGAParser

/**
 * 解析 assets 任务
 */
class DecodeAssetsTask(path: String, callback: SVGAParser.ParseCompletion?) :
        DecodeTask(path, callback) {

    fun getPath(): String = "file:///assets/$url"

    override fun run() {
        try {
            DecodeParseCenter.getFromAssets(url).let { stream ->
                DecodeParseCenter.decodeInputStream(this, stream, true)
            }
        } catch (e: Exception) {
            DecodeParseCenter.invokeErrorCallback(taskCacheKey, e)
        }
    }

}