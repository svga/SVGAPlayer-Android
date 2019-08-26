package com.opensource.svgaplayer.log

/**
 * created by lijun3 on 2019/6/12
 */
interface SLogger {
    fun v(tag: String, msg: String)
    fun i(tag: String, msg: String)
    fun d(tag: String, msg: String)
    fun w(tag: String, msg: String)
    fun e(tag: String, msg: String)
    fun e(tag: String, msg: String, error: Throwable)
}