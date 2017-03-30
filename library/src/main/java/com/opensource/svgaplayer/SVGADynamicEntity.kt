package com.opensource.svgaplayer

import android.graphics.Bitmap
import android.text.TextPaint

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