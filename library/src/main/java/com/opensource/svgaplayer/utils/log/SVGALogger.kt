package com.opensource.svgaplayer.utils.log

/**
 * SVGA logger 配置管理
 **/
object SVGALogger {

    private var mLogger: ILogger? = null

    /**
     * log 接管注入
     */
    fun injectSVGALoggerImp(logImp: ILogger): SVGALogger {
        mLogger = logImp
        return this
    }

    /**
     * 是否开启默认 log 输出
     */
    fun setLogEnabled(isEnabled: Boolean): SVGALogger {
        mLogger = if (isEnabled) {
            DefaultLogCat()
        } else {
            null
        }
        return this
    }

    fun getSVGALogger(): ILogger? {
        return mLogger
    }

    fun isOpenLogger(): Boolean {
        return mLogger != null
    }
}