package com.opensource.svgaplayer.bitmap

/**
 *
 * Create by im_dsd 2020/7/6 16:35
 */
interface BitmapCreator {
    /**
     * 创建 bitmap
     *
     * @param filePath 文件路径
     * @param reqWidth 期望的宽
     * @param reqHeight 期望的高
     */
    fun createBitmap(filePath: String, reqWidth: Int, reqHeight: Int, callback: BitmapCreatorCallback)

    /**
     * 创建 bitmap
     *
     * @param byteArray bitmap byte array
     * @param reqWidth 期望的宽
     * @param reqHeight 期望的高
     */
    fun createBitmap(byteArray: ByteArray, reqWidth: Int, reqHeight: Int, callback: BitmapCreatorCallback)
}