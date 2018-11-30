package com.opensource.svgaplayer.glideplugin

import android.net.Uri
import android.support.annotation.MainThread
import com.bumptech.glide.Priority
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.load.model.StringLoader
import com.bumptech.glide.load.model.UrlUriLoader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipInputStream

/**
 * Created by 张宇 on 2018/11/27.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
internal class SVGAEntityLoader(
    private val actual: ModelLoader<GlideUrl, InputStream>,
    private val cachePath: String
) : ModelLoader<GlideUrl, File> {

    override fun buildLoadData(model: GlideUrl, width: Int, height: Int, options: Options)
        : ModelLoader.LoadData<File>? {
        val actualFetcher = actual.buildLoadData(model, width, height, options)?.fetcher
            ?: return null
        return ModelLoader.LoadData(model, SVGAEntityFetcher(model, actualFetcher, cachePath))
    }

    override fun handles(model: GlideUrl) =
        model.toStringUrl().substringBefore('?').endsWith(".svga") &&
            actual.handles(model)

    internal class SVGAEntityLoaderFactory(private val cachePath: String) : ModelLoaderFactory<GlideUrl, File> {

        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<GlideUrl, File> {
            return SVGAEntityLoader(
                multiFactory.build(GlideUrl::class.java, InputStream::class.java),
                cachePath)
        }

        override fun teardown() {
            //do nothing
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

    private class SVGAEntityFetcher(
        private val model: GlideUrl,
        private val fetcher: DataFetcher<InputStream>,
        private val cachePath: String
    ) : AbsSVGAEntityDecoder(), DataFetcher<File> {

        private val isCanceled = AtomicBoolean()

        override fun getDataClass() = File::class.java

        override fun cleanup() {
            fetcher.cleanup()
        }

        override fun getDataSource() = fetcher.dataSource

        @MainThread
        override fun cancel() {
            isCanceled.set(true)
            fetcher.cancel()
        }

        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in File>) {
            fetcher.loadData(priority, object : DataFetcher.DataCallback<InputStream> {

                override fun onLoadFailed(e: Exception) {
                    callback.onLoadFailed(e)
                }

                override fun onDataReady(data: InputStream?) {
                    if (data == null) {
                        callback.onLoadFailed(NullPointerException("Data is null."))
                    } else {
                        val result = try {
                            decode(data)
                        } catch (e: Exception) {
                            callback.onLoadFailed(e)
                            return
                        }
                        if (result == null || !result.isDirectory) {
                            callback.onLoadFailed(NullPointerException("The result of SVGAEntityFetcher is null."))
                        } else {
                            callback.onDataReady(result)
                        }
                    }
                }
            })
        }

        private fun decode(source: InputStream): File? {
            if (isCanceled.get()) return null

            readHeadAsBytes(source)?.let { sourceHead ->
                if (sourceHead.isZipFormat && !isCanceled.get()) {
                    if (!cacheDir.exists()) {
                        try {
                            cacheDir.mkdirs()
                            unzip(source, cacheDir)
                        } catch (e: Exception) {
                            cacheDir.deleteRecursively()
                            e.printStackTrace()
                        }
                    }

                    return cacheDir
                }
            }
            return null
        }

        private fun unzip(inputStream: InputStream, dir: File) {
            ZipInputStream(inputStream).use { zipInputStream ->
                while (true) {
                    val zipItem = zipInputStream.nextEntry ?: break
                    if (zipItem.name.contains("/")) {
                        continue
                    }
                    val file = File(dir, zipItem.name)
                    FileOutputStream(file).use { fileOutputStream ->
                        val buff = ByteArray(2048)
                        while (true) {
                            val readBytes = zipInputStream.read(buff)
                            if (readBytes <= 0) {
                                break
                            }
                            fileOutputStream.write(buff, 0, readBytes)
                        }
                    }
                    zipInputStream.closeEntry()
                }
            }
        }

        private fun cacheKey(str: String): String {
            val messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.update(str.toByteArray(charset("UTF-8")))
            val digest = messageDigest.digest()
            var sb = ""
            for (b in digest) {
                sb += String.format("%02x", b)
            }
            return sb
        }

        private val cacheDir: File by lazy(LazyThreadSafetyMode.NONE) {
            File(cachePath, cacheKey(model.toStringUrl()))
        }
    }
}