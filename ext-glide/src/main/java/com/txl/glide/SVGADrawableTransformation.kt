package com.txl.glide

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.opensource.svgaplayer.drawer.SVGAAnimationDrawable
import java.security.MessageDigest

class SVGADrawableTransformation(private val wrapped:Transformation<Bitmap>): Transformation<SVGAAnimationDrawable> {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        wrapped.updateDiskCacheKey(messageDigest)
    }

    override fun transform(
        context: Context,
        resource: Resource<SVGAAnimationDrawable>,
        outWidth: Int,
        outHeight: Int,
    ): Resource<SVGAAnimationDrawable> {
        val drawable = resource.get()

        when (wrapped) {
            is CenterCrop -> {
                drawable.scaleType = ImageView.ScaleType.CENTER_CROP
            }
            is CenterInside -> {
                drawable.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
            is FitCenter -> {
                drawable.scaleType = ImageView.ScaleType.FIT_CENTER
            }
        }
        return resource
    }
}