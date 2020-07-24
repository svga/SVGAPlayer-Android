package com.opensource.svgaplayer.utils.log

import android.util.Log

/****
 * Project： SVGAPlayer-Android
 * Author：yangshun@yy.com
 * YY：909041099
 * Created：2020/4/15 11:29
 * Description：
 *
 *
 ****/
class DefaultLogCat : ILogger {
    override fun verbose(tag: String, msg: String) {
        Log.v(tag, msg)
    }

    override fun info(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    override fun debug(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    override fun warn(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    override fun error(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    override fun error(tag: String, error: Throwable) {
        Log.e(tag, "", error)
    }

    override fun error(tag: String, msg: String, error: Throwable) {
        Log.e(tag, msg, error)
    }
}