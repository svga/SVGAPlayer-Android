package com.opensource.svgaplayer.utils.log

/****
 * Project： SVGAPlayer-Android
 * Author：yangshun@yy.com
 * YY：909041099
 * Created：2020/4/15 11:24
 * Description：
 *
 *
 ****/
interface ILogger {
    fun verbose(tag: String, msg: String)
    fun info(tag: String, msg: String)
    fun debug(tag: String, msg: String)
    fun warn(tag: String, msg: String)
    fun error(tag: String, msg: String)
    fun error(tag: String, error: Throwable)
    fun error(tag: String, msg: String, error: Throwable)
}