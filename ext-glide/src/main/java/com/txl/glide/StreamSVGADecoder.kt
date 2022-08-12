package com.txl.glide

import android.content.Context
import android.util.Log
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.drawer.SVGAAnimationDrawable
import com.opensource.svgaplayer.SVGASimpleParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.txl.glide.resource.SVGADrawableResource
import java.util.concurrent.CountDownLatch


class StreamSVGADecoder(
    private val svgaSimpleParser: SVGASimpleParser,
    private val context: Context,
) : ResourceDecoder<SVGALoadKey, SVGAAnimationDrawable> {

    private val tag = StreamSVGADecoder::class.java.simpleName

    private var countDown = CountDownLatch(1)

    private val lock = Object()

    override fun handles(source: SVGALoadKey, options: Options): Boolean {
        return true
    }

    override fun decode(
        source: SVGALoadKey,
        width: Int,
        height: Int,
        options: Options,
    ): Resource<SVGAAnimationDrawable>? {
        countDown = CountDownLatch(1)
        var svga: SVGAVideoEntity? = null
        source.inputStream?.let {
            svga = svgaSimpleParser.decodeFromInputStream(it)
        }
        svga?.let {
            return SVGADrawableResource(SVGAAnimationDrawable(it,
                repeatCount = source.svgaModel.repeatCount,
                repeatMode = source.svgaModel.repeatMode,
                dynamicItem = SVGADynamicEntity()))
        }
        return null
    }
}