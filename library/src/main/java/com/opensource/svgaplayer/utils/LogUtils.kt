package com.opensource.svgaplayer.utils

import android.util.Log

object LogUtils {

    private const val TAG = "SVGALog"

    fun i(msg: String, tag: String = TAG) {
        Log.i(tag, msg)
    }

    fun e(msg: String, tag: String = TAG){
        Log.e(tag, msg)
    }
}