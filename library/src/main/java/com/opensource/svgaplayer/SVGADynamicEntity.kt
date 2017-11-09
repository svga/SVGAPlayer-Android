package com.opensource.svgaplayer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextPaint
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.logging.Handler

/**
 * Created by cuiminghui on 2017/3/30.
 */
class SVGADynamicEntity {

    var dynamicImage: HashMap<String, Bitmap> = hashMapOf()

    var dynamicText: HashMap<String, String> = hashMapOf()

    var dynamicTextPaint: HashMap<String, TextPaint> = hashMapOf()

    fun setDynamicImage(bitmap: Bitmap, forKey: String) {
        this.dynamicImage.put(forKey, bitmap)
    }

    fun setDynamicImage(url: String, forKey: String) {
        val handler = android.os.Handler()
        Thread({
            try {
                (URL(url).openConnection() as? HttpURLConnection)?.let {
                    it.connectTimeout = 20 * 1000
                    it.requestMethod = "GET"
                    it.connect()
                    BitmapFactory.decodeStream(it.inputStream)?.let {
                        handler.post { setDynamicImage(it, forKey) }
                    }
                    it.inputStream.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    fun setDynamicText(text: String, textPaint: TextPaint, forKey: String) {
        this.dynamicText.put(forKey, text)
        this.dynamicTextPaint.put(forKey, textPaint)
    }

    fun clearDynamicObjects() {
        this.dynamicImage.clear()
        this.dynamicText.clear()
        this.dynamicTextPaint.clear()
    }

}