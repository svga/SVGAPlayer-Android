package com.opensource.svgaplayer.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * 通过字节码解码 Bitmap
 *
 * Create by im_dsd 2020/7/7 17:50
 */
internal object SVGABitmapByteArrayDecoder : SVGABitmapDecoder<ByteArray>() {

    override fun onDecode(data: ByteArray, ops: BitmapFactory.Options): Bitmap? {
        return BitmapFactory.decodeByteArray(data, 0, data.count(), ops)
    }
}