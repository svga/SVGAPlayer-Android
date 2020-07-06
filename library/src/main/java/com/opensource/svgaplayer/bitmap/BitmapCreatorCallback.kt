package com.opensource.svgaplayer.bitmap

import android.graphics.Bitmap

/**
 *
 * Create by im_dsd 2020/7/6 17:59
 */
interface BitmapCreatorCallback {
    fun onCreateComplete(bitmap: Bitmap?)
}