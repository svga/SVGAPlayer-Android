package com.opensource.svgaplayer.log

import android.util.Log

/**
 * created by lijun3 on 2019/6/12
 */
class SLogCatSLogger : SLogger {
    override fun v(tag: String, msg: String) {
        Log.v(tag, msg)
    }

    override fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    override fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    override fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    override fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    override fun e(tag: String, msg: String, error: Throwable) {
        Log.e(tag, msg, error)
    }
}