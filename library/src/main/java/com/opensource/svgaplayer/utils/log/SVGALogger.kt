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

    private var openLog: Boolean = false


    private var mLogger: ILogger? = DefaultLogCat()

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
    fun setSVGALogOpen(open: Boolean): SVGALogger {
        openLog = open
        return this
    }

    fun getSVGALogger(): ILogger? {
        return mLogger
    }

    fun isOpenLogger(): Boolean {
        return openLog
    }
}