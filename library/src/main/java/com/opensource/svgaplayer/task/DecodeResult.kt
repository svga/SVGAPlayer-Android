package com.opensource.svgaplayer.task

import com.opensource.svgaplayer.SVGAVideoEntity

class DecodeResult(
        val taskKey: String,
        val entity: SVGAVideoEntity? = null,
        val error: Exception? = null
) {
    fun isSuccess() = error == null
}