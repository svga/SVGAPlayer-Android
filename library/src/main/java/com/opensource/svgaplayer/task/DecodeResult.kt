package com.opensource.svgaplayer.task

import com.opensource.svgaplayer.SVGAVideoEntity

/**
 * 解析结果
 */
class DecodeResult(
        val taskKey: String,
        val entity: SVGAVideoEntity? = null,
        val error: Exception? = null
) {
    fun isSuccess() = error == null
}