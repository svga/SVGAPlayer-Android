package com.opensource.svgaplayer.task

import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.utils.FileDownloader
import java.net.URL

class DecodeUrlTask(url: String, callback: SVGAParser.ParseCompletion?) : DecodeTask(url, callback) {

    override fun run() {
        try {
            if (DecodeParseCenter.hasDiskCached(taskCacheKey)) {
                DecodeParseCenter.decodeFromCacheKey(taskCacheKey)
            } else {
                FileDownloader.resume(URL(url), { stream ->
                    DecodeParseCenter.decodeInputStream(this, stream, true)
                }, { e ->
                    DecodeParseCenter.invokeErrorCallback(taskCacheKey, e)
                })
            }

        } catch (e: Exception) {
            DecodeParseCenter.invokeErrorCallback(taskCacheKey, e)
        }
    }
}