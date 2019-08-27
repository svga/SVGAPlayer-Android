package com.opensource.svgaplayer.log

/**
 * created by lijun3 on 2019/6/12
 */
object SLog : SLogger {

    var sSLogger: SLogger? = SLogCatSLogger()

    override fun v(tag: String, msg: String) {
        sSLogger?.v(tag, msg)
    }

    override fun i(tag: String, msg: String) {
        sSLogger?.i(tag, msg)
    }

    override fun d(tag: String, msg: String) {
        sSLogger?.d(tag, msg)
    }

    override fun w(tag: String, msg: String) {
        sSLogger?.w(tag, msg)
    }

    override fun e(tag: String, msg: String) {
        sSLogger?.e(tag, msg)
    }

    override fun e(tag: String, msg: String, error: Throwable) {
        sSLogger?.e(tag, msg, error)
    }
}