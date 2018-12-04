package com.opensource.svgaplayer.glideplugin

import android.util.Log
import java.io.InputStream

/**
 * Created by 张宇 on 2018/11/27.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
internal abstract class AbsSVGAEntityDecoder {

    protected val ByteArray.isZipFormat
        get() = this.size >= 4 &&
            this[0].toInt() == 80 &&
            this[1].toInt() == 75 &&
            this[2].toInt() == 3 &&
            this[3].toInt() == 4

    /**
     * Note: don't close the inputStream!
     */
    protected fun readHeadAsBytes(inputStream: InputStream): ByteArray? = try {
        val byteArray = ByteArray(4)
        val count = inputStream.read(byteArray, 0, 4)
        if (count <= 0) {
            null
        } else {
            byteArray
        }
    } catch (e: Exception) {
        null
    } finally {
        inputStream.reset()
    }

    protected inline fun <T : Any> attempt(action: () -> T?): T? {
        return try {
            action()
        } catch (e: Throwable) {
            handleError(e)
            null
        }
    }

    protected fun handleError(e: Throwable) = Log.e("SVGAPlayer", e.message, e)
}