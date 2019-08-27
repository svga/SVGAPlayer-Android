package com.opensource.svgaplayer.threadpool

import android.os.Handler
import android.os.Looper

internal class HandlEx(val name: String? = null, looper: Looper?, callback: Callback?) : Handler(looper, callback) {

    override fun toString(): String {
        return "HandlerEx ($name) {}"
    }
}
