package com.opensource.svgaplayer.glideplugin

import com.bumptech.glide.RequestManager
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAVideoEntity

/**
 * Created by 张宇 on 2018/11/26.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
const val BUCKET_SVGA = "SVGA"

fun RequestManager.asSVGA() = `as`(SVGAVideoEntity::class.java)

fun RequestManager.asSVGADrawable() = `as`(SVGADrawable::class.java)