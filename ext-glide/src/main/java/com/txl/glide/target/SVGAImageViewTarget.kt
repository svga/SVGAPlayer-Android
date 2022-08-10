package com.txl.glide.target

import android.widget.ImageView
import com.bumptech.glide.request.target.ImageViewTarget
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView

class SVGAImageViewTarget(view: SVGAImageView?) : ImageViewTarget<SVGADrawable>(view) {
    override fun setResource(resource: SVGADrawable?) {
        view?.setImageDrawable(resource)
    }
}