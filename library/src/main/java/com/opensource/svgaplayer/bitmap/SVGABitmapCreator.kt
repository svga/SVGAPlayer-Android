package com.opensource.svgaplayer.bitmap

import com.opensource.svgaplayer.utils.BitmapUtils

/**
 * bitmap 构建器具
 *
 * Create by im_dsd 2020/7/6 16:40
 */
object SVGABitmapCreator {
    private var mCreator: BitmapCreator? = null;

    fun createBitmap(filePath: String, reqWidth: Int, reqHeight: Int, callback: BitmapCreatorCallback) {
        if (filePath.isEmpty()) {
            callback.onCreateComplete(null)
        }
        if (mCreator != null) {
            mCreator?.createBitmap(filePath, reqWidth, reqHeight, callback)
        } else {
            val bitmap = BitmapUtils.decodeSampledBitmapFromFile(filePath, reqWidth, reqHeight)
            callback.onCreateComplete(bitmap)
        }
    }

    fun createBitmap(byteArray: ByteArray, reqWidth: Int, reqHeight: Int, callback: BitmapCreatorCallback) {
        if (byteArray.isEmpty()) {
            return
        }
        if (mCreator != null) {
            mCreator?.createBitmap(byteArray, reqWidth, reqHeight, callback)
        } else {
            val bitmap = BitmapUtils.decodeSampledBitmapFromByteArray(byteArray, reqWidth, reqHeight)
            callback.onCreateComplete(bitmap)
        }
    }

    /**
     * 设置属于自己的构造器，但是要保证在 bitmap 使用期间不会调用 bitmap.recycle()
     */
    fun setBitmapCreator(creator: BitmapCreator) {
        mCreator = creator
    }
}