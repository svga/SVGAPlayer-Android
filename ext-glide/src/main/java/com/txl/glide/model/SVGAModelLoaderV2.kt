package com.txl.glide.model

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.txl.glide.SVGALoadKey
import com.txl.glide.model.fetcher.SVGAFetcher
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MultiSVGAModelLoaderV2 constructor(
       private val multiFactory: MultiModelLoaderFactory,
) : ModelLoader<SVGAModel, SVGALoadKey> {

    companion object {
        fun init(context: Context) {
            Glide.get(context).registry.prepend(SVGAModel::class.java,
                SVGALoadKey::class.java,
                MultSVGAStringLoaderFactory())
        }
    }


    override fun buildLoadData(
        model: SVGAModel,
        width: Int,
        height: Int,
        options: Options,
    ): LoadData<SVGALoadKey>? {
        var loadData:LoadData<InputStream>? = null
        loadData = when(model.typeClass){
            SVGALoadType.String->{
                val stringModelLoader = multiFactory.build(
                    String::class.java,
                    InputStream::class.java)
                stringModelLoader.buildLoadData(model.path as String, width, height, options)
            }
            SVGALoadType.Uri->{
                val uriModelLoader = multiFactory.build(
                    Uri::class.java,
                    InputStream::class.java)
                uriModelLoader.buildLoadData(model.path as Uri, width, height, options)
            }
        }
        return if(loadData == null){
            null
        }else{
            LoadData(loadData.sourceKey,
                loadData.alternateKeys,
                SVGAFetcher(model, loadData))
        }
    }

    override fun handles(model: SVGAModel): Boolean {
        return true
    }

    private class MultSVGAStringLoaderFactory : ModelLoaderFactory<SVGAModel, SVGALoadKey> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<SVGAModel, SVGALoadKey> {
            return MultiSVGAModelLoaderV2(multiFactory)
        }

        override fun teardown() {

        }
    }
}
