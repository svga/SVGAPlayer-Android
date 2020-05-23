package com.opensource.svgaplayer.utils

import android.content.Context
import java.io.InputStream

object ResUtil {

    fun getFromAssets(context: Context, path: String): InputStream {
        return context.assets.open(path)
    }
}