package com.opensource.svgaplayer

import com.opensource.svgaplayer.log.SLog
import com.opensource.svgaplayer.log.SLogger

/**
 * Created by guojunjie on 2019-08-26.
 * svga 的初始化操作
 * 目前只是为了注入log
 */
object SVGAFactory {

    fun init(
        slog: SLogger? = null
    ) {
        if (slog != null) {
            SLog.sSLogger = slog
        }
    }
}