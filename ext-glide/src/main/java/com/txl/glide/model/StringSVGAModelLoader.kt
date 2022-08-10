package com.txl.glide.model

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.txl.glide.SVGALoadKey
import com.txl.glide.model.fetcher.SVGAFetcher
import java.io.InputStream

class StringSVGAModelLoader(modelLoader: ModelLoader<String, InputStream>?) :
    SVGALoader<String>(modelLoader) {

    companion object{
        fun init(context:Context){
            Glide.get(context).registry.prepend(String::class.java, SVGALoadKey::class.java, SVGAStringLoaderFactory())
        }
    }

    override fun buildLoadData(
        model: String,
        width: Int,
        height: Int,
        options: Options,
    ): LoadData<SVGALoadKey>? {
        val loadData = modelLoader.buildLoadData(model, width, height, options) ?: return null
        val isvgaModel = SVGAModel(model,SVGALoadType.String)
        return LoadData(loadData.sourceKey,
            loadData.alternateKeys,
            SVGAFetcher(isvgaModel, loadData))
    }

    private class SVGAStringLoaderFactory : ModelLoaderFactory<String, SVGALoadKey> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, SVGALoadKey> {
            val modelLoader = multiFactory.build(
                String::class.java,
                InputStream::class.java)
            return StringSVGAModelLoader(modelLoader)
        }

        override fun teardown() {

        }
    }

    override fun handles(model: String): Boolean {
        return model.endsWith(".svga")
    }
}