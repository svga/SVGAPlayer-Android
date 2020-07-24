package com.opensource.svgaplayer.utils.log

/**
 * 日志输出
 */
internal object LogUtils {

    private const val TAG = "SVGALog"

    fun verbose(tag: String = TAG, msg: String) {
        SVGALogger.getSVGALogger()?.verbose(tag, msg)
    }

    fun info(tag: String = TAG, msg: String) {
        SVGALogger.getSVGALogger()?.info(tag, msg)
    }

    fun debug(tag: String = TAG, msg: String) {
        SVGALogger.getSVGALogger()?.debug(tag, msg)
    }

    fun warn(tag: String = TAG, msg: String) {
        SVGALogger.getSVGALogger()?.warn(tag, msg)
    }

    fun error(tag: String = TAG, msg: String) {
        SVGALogger.getSVGALogger()?.error(tag, msg)
    }

    fun error(tag: String = TAG, msg: String, error: Throwable) {
        SVGALogger.getSVGALogger()?.error(tag, msg, error)
    }

    fun error(tag: String, error: Throwable) {
        SVGALogger.getSVGALogger()?.error(tag, error)
    }
}