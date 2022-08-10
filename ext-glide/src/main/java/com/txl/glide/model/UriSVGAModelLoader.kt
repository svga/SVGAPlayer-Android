package com.txl.glide.model

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.txl.glide.SVGALoadKey
import com.txl.glide.model.fetcher.SVGAFetcher
import java.io.InputStream

class UriSVGAModelLoader(modelLoader: ModelLoader<Uri, InputStream>?) : SVGALoader<Uri>(modelLoader) {

    companion object{
        fun init(context:Context){
            Glide.get(context).registry.prepend(Uri::class.java, SVGALoadKey::class.java, SVGAUriLoaderFactory())
        }
    }

    override fun buildLoadData(
        model: Uri,
        width: Int,
        height: Int,
        options: Options,
    ): LoadData<SVGALoadKey>? {
        val loadData = modelLoader.buildLoadData(model, width, height, options) ?: return null
        val isvgaModel = SVGAModel(model,SVGALoadType.Uri)
        return LoadData(loadData.sourceKey,
            loadData.alternateKeys,
            SVGAFetcher(isvgaModel, loadData))
    }

    private class SVGAUriLoaderFactory : ModelLoaderFactory<Uri, SVGALoadKey> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Uri, SVGALoadKey> {
            val modelLoader = multiFactory.build(
                Uri::class.java,
                InputStream::class.java)
            return UriSVGAModelLoader(modelLoader)
        }

        override fun teardown() {

        }
    }

    override fun handles(model: Uri): Boolean {
        return model.toString().endsWith(".svga")
    }
}