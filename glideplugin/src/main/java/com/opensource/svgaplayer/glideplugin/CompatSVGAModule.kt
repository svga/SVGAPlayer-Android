package com.opensource.svgaplayer.glideplugin

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry

/**
 * Created by 张宇 on 2018/11/30.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 *
 * GlideModule Compatible with Android Manifest Declarations.
 *
 * @see SVGAModule
 */
@Deprecated(message = "Replaced by [SVGAModule] for Applications that use Glide's annotations.")
class CompatSVGAModule : com.bumptech.glide.module.GlideModule {

    private val actualModule by lazy(LazyThreadSafetyMode.NONE) { SVGAModule() }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        //Do nothing
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        actualModule.registerComponents(context, glide, registry)
    }
}