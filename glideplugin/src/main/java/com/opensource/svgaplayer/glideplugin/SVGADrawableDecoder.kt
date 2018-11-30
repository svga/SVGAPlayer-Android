package com.opensource.svgaplayer.glideplugin

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGADynamicEntity
import java.io.InputStream

/**
 * Created by 张宇 on 2018/11/26.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
internal class SVGADrawableDecoder(private val actual: SVGAEntityStreamDecoder) :
    ResourceDecoder<InputStream, SVGADrawable> {

    override fun handles(source: InputStream, options: Options): Boolean {
        return actual.handles(source, options)
    }

    override fun decode(source: InputStream, width: Int, height: Int, options: Options): SVGADrawableResource? {
        val entityRes = actual.decode(source, width, height, options)
            ?: return null
        return SVGADrawableResource(
            SVGADrawable(entityRes.get(), SVGADynamicEntity()), entityRes)
    }

}