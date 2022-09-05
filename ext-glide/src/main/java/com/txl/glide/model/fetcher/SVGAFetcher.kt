package com.txl.glide.model.fetcher

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.txl.glide.SVGALoadKey
import com.txl.glide.model.SVGAModel
import java.io.InputStream

internal class SVGAFetcher(
    private val svgaModel: SVGAModel,
    private val loadData: LoadData<InputStream>,
) : DataFetcher<SVGALoadKey> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in SVGALoadKey?>) {
        loadData.fetcher.loadData(priority, object : DataFetcher.DataCallback<InputStream?> {
            override fun onDataReady(data: InputStream?) {
                val svgaLoadKey = SVGALoadKey(svgaModel, data)
                callback.onDataReady(svgaLoadKey)
            }

            override fun onLoadFailed(e: Exception) {
                callback.onLoadFailed(e)
            }
        })
    }

    override fun cleanup() {
        loadData.fetcher.cleanup()
    }

    override fun cancel() {
        loadData.fetcher.cancel()
    }

    override fun getDataClass(): Class<SVGALoadKey> {
        return SVGALoadKey::class.java
    }

    override fun getDataSource(): DataSource {
//        return loadData.fetcher.dataSource
        return DataSource.REMOTE
    }
}