package com.opensource.svgaplayer.utils.log

/**
 * log 外部接管接口
 **/
interface ILogger {
    fun verbose(tag: String, msg: String)
    fun info(tag: String, msg: String)
    fun debug(tag: String, msg: String)
    fun warn(tag: String, msg: String)
    fun error(tag: String, msg: String?, error: Throwable?)
}