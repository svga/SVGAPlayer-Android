package com.opensource.svgaplayer.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

/**
 *
 * Create by im_dsd 2020/7/4 21:10
 */
object BitmapUtils {

    fun decodeSampledBitmapFromFile(
            fileName: String,
            reqWidth: Int,
            reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            inPreferredConfig = Bitmap.Config.RGB_565
            BitmapFactory.decodeFile(fileName, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false
            BitmapFactory.decodeFile(fileName, this)
        }
    }

    fun decodeSampledBitmapFromByteArray(
            byteArray: ByteArray,
            reqWidth: Int,
            reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            inPreferredConfig = Bitmap.Config.RGB_565
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.count(), this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.count(), this)
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (reqHeight <= 0 || reqWidth <= 0) {
            Log.d("BitmapUtils", "inSampleSize:$inSampleSize height:$height width$width  reqHeight$reqHeight reqWidth$reqWidth" )
            return inSampleSize
        }
        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        Log.d("BitmapUtils", "inSampleSize:$inSampleSize height:$height width$width  reqHeight$reqHeight reqWidth$reqWidth" )
        return inSampleSize
    }

}