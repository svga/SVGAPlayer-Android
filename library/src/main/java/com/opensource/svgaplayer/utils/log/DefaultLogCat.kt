package com.opensource.svgaplayer.utils.log

import android.util.Log

/**
 * 内部默认 ILogger 接口实现
 */
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

    override fun error(tag: String, msg: String?, error: Throwable?) {
        Log.e(tag, msg, error)
    }
}