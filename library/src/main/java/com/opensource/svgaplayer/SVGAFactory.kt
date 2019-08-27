package com.opensource.svgaplayer

import com.opensource.svgaplayer.log.SLog
import com.opensource.svgaplayer.log.SLogger
import com.opensource.svgaplayer.threadpool.SVGATaskExecutor
import java.util.concurrent.ThreadPoolExecutor

/**
 * Created by guojunjie on 2019-08-26.
 * svga 的初始化操作
 * 目前只是为了注入log
 */
object SVGAFactory {

    fun init(
        slog: SLogger? = null,
        sThreadPoolExecutor: ThreadPoolExecutor? = null
    ) {
        if (slog != null) {
            SLog.sSLogger = slog
        }
        if (sThreadPoolExecutor != null) {
            SVGATaskExecutor.setThreadPoolExecutor(sThreadPoolExecutor)
        }
    }
}