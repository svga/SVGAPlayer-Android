package com.txl.glide

import android.content.Context
import android.net.Uri
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.request.target.Target
import com.opensource.svgaplayer.SVGACache
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGASimpleParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.opensource.svgaplayer.drawer.SVGAAnimationDrawable
import com.txl.glide.resource.SVGADrawableResource
import java.util.concurrent.CountDownLatch


class SVGALoadKeySVGADecoder(
    private val svgaSimpleParser: SVGASimpleParser,
    private val context: Context,
) : ResourceDecoder<SVGALoadKey, SVGAAnimationDrawable> {

    private val tag = SVGALoadKeySVGADecoder::class.java.simpleName

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
            val cacheKey = when (source.svgaModel.path) {
                is String -> {
                    SVGACache.buildCacheKey(source.svgaModel.path)
                }
                is Uri -> {
                    SVGACache.buildCacheKey(source.svgaModel.path.toString())
                }
                else -> {
                    source.svgaModel.path.toString()
                }
            }
            val requestedWidth = if (width == Target.SIZE_ORIGINAL) {
                0
            } else {
                width
            }
            val requestedHeight = if (height == Target.SIZE_ORIGINAL) {
                0
            } else {
                height
            }
            svga = svgaSimpleParser.decodeFromInputStream(
                it,
                cacheKey,
                requestedWidth,
                requestedHeight
            )
        }
        svga?.let {
            return SVGADrawableResource(
                SVGAAnimationDrawable(
                    it,
                    repeatCount = source.svgaModel.repeatCount,
                    repeatMode = source.svgaModel.repeatMode,
                    dynamicItem = SVGADynamicEntity()
                )
            )
        }
        return null
    }
}