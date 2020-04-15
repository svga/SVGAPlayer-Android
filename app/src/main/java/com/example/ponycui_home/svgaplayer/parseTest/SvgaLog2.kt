package com.example.ponycui_home.svgaplayer.parseTest

import android.util.Log
import com.opensource.svgaplayer.utils.log.ILogger

/****
 * Project： SVGAPlayer-Android
 * Author：yangshun@yy.com
 * YY：909041099
 * Created：2020/4/15 15:51
 * Description：
 *
 *
 ****/
class SvgaLog2 : ILogger {
    override fun verbose(
        tag: String, msg: String
    ) {
        Log.v(tag, msg.formatCompat())
    }

    override fun info(tag: String, msg: String) {
        Log.i(tag, msg.formatCompat())
    }

    override fun debug(tag: String, msg: String) {
        Log.d(tag, msg.formatCompat())
    }

    override fun warn(tag: String, msg: String) {
        Log.w(tag, msg.formatCompat())
    }

    override fun error(tag: String, msg: String) {
        Log.e(tag, msg.formatCompat())
    }

    override fun error(tag: String, error: Throwable) {
        error.printStackTrace()
    }

    override fun error(tag: String, msg: String, error: Throwable) {
        Log.v(tag, msg)
        error.printStackTrace()
    }
}

private fun String.formatCompat(vararg args: Any?): String {
    return try {
        if (args.isEmpty()) this else String.format(this, *args)
    } catch (e: Exception) {
        this
    }
}

