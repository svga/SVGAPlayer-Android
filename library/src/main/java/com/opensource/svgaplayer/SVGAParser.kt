package com.opensource.svgaplayer

import com.opensource.svgaplayer.task.DecodeParseCenter
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

/**
 * Created by PonyCui 16/6/18.
 */

class SVGAParser() {

    interface ParseCompletion {

        fun onComplete(videoItem: SVGAVideoEntity)
        fun onError()
    }

    companion object {
        private var threadPoolExecutor = Executors.newCachedThreadPool()

        fun setThreadPoolExecutor(executor: ThreadPoolExecutor) {
            threadPoolExecutor = executor
        }

        fun getThreadPoolExecutor(): ExecutorService {
            if (threadPoolExecutor == null) {
                threadPoolExecutor = Executors.newCachedThreadPool()
            } else if (threadPoolExecutor.isShutdown) {
                threadPoolExecutor = Executors.newCachedThreadPool()
            }
            return threadPoolExecutor
        }

        private var mShareParser = SVGAParser()
        fun shareParser(): SVGAParser {
            return mShareParser
        }
    }

    /**
     * @deprecated from 2.4.0
     */
    @Deprecated(
            "This method has been deprecated from 2.4.0.",
            ReplaceWith("this.decodeFromAssets(assetsName, callback)")
    )
    fun parse(assetsName: String, callback: ParseCompletion?) {
        this.decodeFromAssets(assetsName, callback)
    }

    /**
     * @deprecated from 2.4.0
     */
    @Deprecated(
            "This method has been deprecated from 2.4.0.",
            ReplaceWith("this.decodeFromURL(url, callback)")
    )
    fun parse(url: URL, callback: ParseCompletion?) {
        this.decodeFromURL(url, callback)
    }

    fun decodeFromAssets(name: String, callback: ParseCompletion?) {
        DecodeParseCenter.addTask(name, callback)
    }

    fun decodeFromURL(url: URL, callback: ParseCompletion?) {
        DecodeParseCenter.addTask(url, callback)
    }

}