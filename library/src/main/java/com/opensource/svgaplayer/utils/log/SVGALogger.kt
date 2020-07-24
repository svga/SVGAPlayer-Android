package com.opensource.svgaplayer.utils.log

/****
 * Project： SVGAPlayer-Android
 * Author：yangshun@yy.com
 * YY：909041099
 * Created：2020/4/15 11:28
 * Description：
 * SVGA Global logger config
 *
 ****/
object SVGALogger {

    private var mLogger: ILogger? = null

    /****
     * inject logger implement
     */
    fun injectSVGALoggerImp(logImp: ILogger): SVGALogger {
        mLogger = logImp
        return this
    }

    /****
     * set logger switch
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