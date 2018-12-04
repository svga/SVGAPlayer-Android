package com.opensource.svgaplayer.glideplugin

import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import java.io.InputStream

/**
 * Created by 张宇 on 2018/11/27.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
internal class SVGAEntityUrlLoader(
    actual: ModelLoader<GlideUrl, InputStream>,
    cachePath: String
) : SVGAEntityLoader<GlideUrl>(actual, cachePath) {

    override fun handles(model: GlideUrl) =
        model.toStringUrl().substringBefore('?').endsWith(".svga") &&
            super.handles(model)

    override fun toStringKey(model: GlideUrl): String = model.toStringUrl()
}

