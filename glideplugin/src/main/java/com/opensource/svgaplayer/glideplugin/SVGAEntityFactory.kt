package com.opensource.svgaplayer.glideplugin

import android.net.Uri
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.load.model.StringLoader
import com.bumptech.glide.load.model.UrlUriLoader
import java.io.File
import java.io.InputStream

/**
 * Created by 张宇 on 2018/12/3.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
internal class SVGAUrlLoaderFactory(private val cachePath: String) : ModelLoaderFactory<GlideUrl, File> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<GlideUrl, File> {
        return SVGAEntityUrlLoader(
            multiFactory.build(GlideUrl::class.java, InputStream::class.java),
            cachePath)
    }

    override fun teardown() {
        //do nothing
    }
}

internal class SVGAAssetLoaderFactory(private val cachePath: String) : ModelLoaderFactory<Uri, File> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Uri, File> {
        return SVGAEntityAssetLoader(
            multiFactory.build(Uri::class.java, InputStream::class.java),
            cachePath
        )
    }

    override fun teardown() {
        //Do Nothing
    }
}

internal class SVGAStringLoaderFactory : ModelLoaderFactory<String, File> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, File> {
        return StringLoader(multiFactory.build(Uri::class.java, File::class.java))
    }

    override fun teardown() {
        //Do nothing
    }
}

internal class SVGAUriLoaderFactory : ModelLoaderFactory<Uri, File> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Uri, File> {
        return UrlUriLoader(multiFactory.build(GlideUrl::class.java, File::class.java))
    }

    override fun teardown() {
        //Do Nothing
    }

}