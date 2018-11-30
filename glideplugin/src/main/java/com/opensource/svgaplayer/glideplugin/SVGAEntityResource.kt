package com.opensource.svgaplayer.glideplugin

import com.bumptech.glide.load.engine.Resource
import com.opensource.svgaplayer.SVGAVideoEntity

/**
 * Created by 张宇 on 2018/11/26.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
class SVGAEntityResource(private val entity: SVGAVideoEntity, private val size: Int) :
    Resource<SVGAVideoEntity> {

    override fun getResourceClass() = SVGAVideoEntity::class.java

    override fun get(): SVGAVideoEntity = entity

    override fun getSize() = size

    override fun recycle() {
        //do nothing
    }
}