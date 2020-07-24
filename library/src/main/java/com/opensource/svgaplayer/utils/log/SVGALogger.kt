package com.opensource.svgaplayer.utils.log

/**
 * SVGA logger 配置管理
 **/
object SVGALogger {

    private var mLogger: ILogger? = DefaultLogCat()
    private var isLogEnabled = false

    /**
     * log 接管注入
     */
    fun injectSVGALoggerImp(logImp: ILogger): SVGALogger {
        mLogger = logImp
        return this
    }

    /**
     * 设置是否开启 log
     */
    fun setLogEnabled(isEnabled: Boolean): SVGALogger {
        isLogEnabled = isEnabled
        return this
    }

    /**
     * 获取当前 ILogger 实现类
     */
    fun getSVGALogger(): ILogger? {
        return mLogger
    }

    /**
     * 是否开启 log
     */
    fun isLogEnabled(): Boolean {
        return isLogEnabled
    }
}