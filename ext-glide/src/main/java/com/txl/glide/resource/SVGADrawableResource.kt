package com.txl.glide.resource

import com.bumptech.glide.load.resource.drawable.DrawableResource
import com.opensource.svgaplayer.drawer.SVGAAnimationDrawable

class SVGADrawableResource(private val drawable: SVGAAnimationDrawable?) : DrawableResource<SVGAAnimationDrawable>(drawable) {
    override fun getResourceClass(): Class<SVGAAnimationDrawable> {
        return SVGAAnimationDrawable::class.java
    }

    override fun getSize(): Int {
        // FIXME: 查看如何计算
        return 1024
    }

    override fun recycle() {
        drawable.recycle()
    }
}