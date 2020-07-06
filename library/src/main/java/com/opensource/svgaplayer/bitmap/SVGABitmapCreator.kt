package com.opensource.svgaplayer.bitmap

import android.graphics.Bitmap
import com.opensource.svgaplayer.utils.BitmapUtils

/**
 * bitmap 构建器具
 *
 * Create by im_dsd 2020/7/6 16:40
 */
object SVGABitmapCreator {
    private var mCreator: BitmapCreator? = null;

    fun createBitmap(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (filePath.isEmpty()) {
            return null
        }
        if (mCreator != null) {
            return mCreator?.createBitmap(filePath, reqWidth, reqHeight)
        }
        return BitmapUtils.decodeSampledBitmapFromFile(filePath, reqWidth, reqHeight)
    }

    fun createBitmap(byteArray: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (mCreator != null) {
            return mCreator?.createBitmap(byteArray, reqWidth, reqHeight)
        }
        return BitmapUtils.decodeSampledBitmapFromByteArray(byteArray, reqWidth, reqHeight)
    }

    /**
     * 设置属于自己的构造器，但是要保证在 bitmap 使用期间不会调用 bitmap.recycle()
     */
    fun setBitmapCreator(creator: BitmapCreator) {
        mCreator = creator
    }
}