package com.opensource.svgaplayer

import com.opensource.svgaplayer.log.SLog
import com.opensource.svgaplayer.log.SLogger
import com.opensource.svgaplayer.threadpool.SVGATaskExecutor
import java.util.concurrent.ThreadPoolExecutor

/**
 * Created by guojunjie on 2019-08-26.
 * svga 的初始化操作， 通常是做一些配置操作
 * 目前主要的作用是：
 * 1.将log 托管
 * 2.将threadpool 托管
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