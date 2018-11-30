package com.opensource.svgaplayer.glideplugin

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.ImageViewTargetFactory
import com.bumptech.glide.request.target.ViewTarget
import com.opensource.svgaplayer.SVGAImageView

/**
 * Created by 张宇 on 2018/11/28.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
internal class SVGAImageViewTargetFactory : ImageViewTargetFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <Z : Any> buildTarget(view: ImageView, clazz: Class<Z>): ViewTarget<ImageView, Z> {
        if (view is SVGAImageView) {
            return SVGADrawableImageViewTarget(view) as ViewTarget<ImageView, Z>
        }
        return super.buildTarget(view, clazz)
    }

    private class SVGADrawableImageViewTarget(private val imageView: SVGAImageView) :
        DrawableImageViewTarget(imageView) {

        override fun setResource(resource: Drawable?) {
            if (resource != null) {
                imageView.setImageDrawable(resource)
                imageView.startAnimation()
            }
        }
    }
}