package com.opensource.svgaplayer.glideplugin

import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.annotation.GlideExtension
import com.bumptech.glide.annotation.GlideType
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAVideoEntity


/**
 * Created by 张宇 on 2018/11/30.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
@GlideExtension
object SVGATypeExtension {

    @JvmStatic
    @GlideType(SVGADrawable::class)
    fun asSVGADrawable(requestBuilder: RequestBuilder<SVGADrawable>) {
    }

    @JvmStatic
    @GlideType(SVGAVideoEntity::class)
    fun asSVGA(requestBuilder: RequestBuilder<SVGAVideoEntity>) {
    }
}