package com.opensource.svgaplayer

import android.content.Context
import android.net.http.HttpResponseCache
import android.os.Handler
import android.util.Log
import com.opensource.svgaplayer.proto.MovieEntity
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.*
import java.util.zip.Inflater
import java.util.zip.ZipInputStream

/**
 * Created by PonyCui 16/6/18.
 */

private var fileLock: Int = 0

class SVGAParser(private val context: Context) {

    interface ParseCompletion {

        fun onComplete(videoItem: SVGAVideoEntity)
        fun onError()

    }

    open class FileDownloader {

        var noCache = false

        open fun resume(url: URL, complete: (inputStream: InputStream) -> Unit, failure: (e: Exception) -> Unit): () -> Unit {
            var cancelled = false
            val cancelBlock = {
                cancelled = true
            }
            Thread {
                try {
                    if (HttpResponseCache.getInstalled() == null && !noCache) {
                        Log.e("SVGAParser", "SVGAParser can not handle cache before install HttpResponseCache. see https://github.com/yyued/SVGAPlayer-Android#cache")
                        Log.e("SVGAParser", "在配置 HttpResponseCache 前 SVGAParser 无法缓存. 查看 https://github.com/yyued/SVGAPlayer-Android#cache ")
                    }
                    (url.openConnection() as? HttpURLConnection)?.let {
                        it.connectTimeout = 20 * 1000
                        it.requestMethod = "GET"
                        it.connect()
                        it.inputStream.use { inputStream ->
                            ByteArrayOutputStream().use { outputStream ->
                                val buffer = ByteArray(4096)
                                var count: Int
                                while (true) {
                                    if (cancelled) {
                                        break
                                    }
                                    count = inputStream.read(buffer, 0, 4096)
                                    if (count == -1) {
                                        break
                                    }
                                    outputStream.write(buffer, 0, count)
                                }
                                if (cancelled) {
                                    return@Thread
                                }
                                ByteArrayInputStream(outputStream.toByteArray()).use {
                                    complete(it)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    failure(e)
                }
            }.start()
            return cancelBlock
        }

    }

    var fileDownloader = FileDownloader()
    private var threadPoolBlockingQueue = LinkedBlockingQueue<Runnable>()
    private var threadPoolExecutor = ThreadPoolExecutor(3, 10, 60000, TimeUnit.MILLISECONDS, this.threadPoolBlockingQueue)

    protected fun finalize() {
        threadPoolExecutor.shutdown()
    }

    fun decodeFromAssets(name: String, callback: ParseCompletion) {
        try {
            context.assets.open(name)?.let {
                this.decodeFromInputStream(it, buildCacheKey("file:///assets/$name"), callback, true)
            }
        }
        catch (e: java.lang.Exception) {
            this.invokeErrorCallback(e, callback)
        }
    }

    fun decodeFromURL(url: URL, callback: ParseCompletion): (() -> Unit)? {
        if (this.isCached(buildCacheKey(url))) {
            threadPoolExecutor.execute {
                this.decodeFromCacheKey(buildCacheKey(url), callback)
            }
            return null
        }
        else {
            return fileDownloader.resume(url, {
                this.decodeFromInputStream(it, this.buildCacheKey(url), callback)
            }, {
                this.invokeErrorCallback(it, callback)
            })
        }
    }

    fun decodeFromInputStream(inputStream: InputStream, cacheKey: String, callback: ParseCompletion, closeInputStream: Boolean = false) {
        threadPoolExecutor.execute {
            try {
                readAsBytes(inputStream)?.let { bytes ->
                    if (bytes.size > 4 && bytes[0].toInt() == 80 && bytes[1].toInt() == 75 && bytes[2].toInt() == 3 && bytes[3].toInt() == 4) {
                        if (!buildCacheDir(cacheKey).exists()) {
                            ByteArrayInputStream(bytes).use {
                                unzip(it, cacheKey)
                            }
                        }
                        this.decodeFromCacheKey(cacheKey, callback)
                    }
                    else {
                        inflate(bytes)?.let {
                            val videoItem = SVGAVideoEntity(MovieEntity.ADAPTER.decode(it), File(cacheKey))
//                            videoItem.prepare {
                                this.invokeCompleteCallback(videoItem, callback)
//                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                this.invokeErrorCallback(e, callback)
            } finally {
                if (closeInputStream) {
                    inputStream.close()
                }
            }
        }
    }

    /**
     * @deprecated from 2.4.0
     */
    @Deprecated("This method has been deprecated from 2.4.0.", ReplaceWith("this.decodeFromAssets(assetsName, callback)"))
    fun parse(assetsName: String, callback: ParseCompletion) {
        this.decodeFromAssets(assetsName, callback)
    }

    /**
     * @deprecated from 2.4.0
     */
    @Deprecated("This method has been deprecated from 2.4.0.", ReplaceWith("this.decodeFromURL(url, callback)"))
    fun parse(url: URL, callback: ParseCompletion) {
        this.decodeFromURL(url, callback)
    }

    /**
     * @deprecated from 2.4.0
     */
    @Deprecated("This method has been deprecated from 2.4.0.", ReplaceWith("this.decodeFromInputStream(inputStream, cacheKey, callback, closeInputStream)"))
    fun parse(inputStream: InputStream, cacheKey: String, callback: ParseCompletion, closeInputStream: Boolean = false) {
        this.decodeFromInputStream(inputStream, cacheKey, callback, closeInputStream)
    }

    private fun invokeCompleteCallback(videoItem: SVGAVideoEntity, callback: ParseCompletion) {
        Handler(context.mainLooper).post {
            callback.onComplete(videoItem)
        }
    }

    private fun invokeErrorCallback(e: java.lang.Exception, callback: ParseCompletion) {
        e.printStackTrace()
        Handler(context.mainLooper).post {
            callback.onError()
        }
    }

    private fun isCached(cacheKey: String): Boolean {
        return buildCacheDir(cacheKey).exists()
    }

    private fun decodeFromCacheKey(cacheKey: String, callback: ParseCompletion) {
        try {
            val cacheDir = File(context.cacheDir.absolutePath + "/" + cacheKey + "/")
            File(cacheDir, "movie.binary").takeIf { it.isFile }?.let { binaryFile ->
                try {
                    FileInputStream(binaryFile).use {
                        this.invokeCompleteCallback(SVGAVideoEntity(MovieEntity.ADAPTER.decode(it), cacheDir), callback)
                    }
                } catch (e: Exception) {
                    cacheDir.delete()
                    binaryFile.delete()
                    throw e
                }
            }
            File(cacheDir, "movie.spec").takeIf { it.isFile }?.let { jsonFile ->
                try {
                    FileInputStream(jsonFile).use { fileInputStream ->
                        ByteArrayOutputStream().use { byteArrayOutputStream ->
                            val buffer = ByteArray(2048)
                            while (true) {
                                val size = fileInputStream.read(buffer, 0, buffer.size)
                                if (size == -1) {
                                    break
                                }
                                byteArrayOutputStream.write(buffer, 0, size)
                            }
                            byteArrayOutputStream.toString().let {
                                JSONObject(it).let {
                                    this.invokeCompleteCallback(SVGAVideoEntity(it, cacheDir), callback)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    cacheDir.delete()
                    jsonFile.delete()
                    throw e
                }
            }
        } catch (e: Exception) {
            this.invokeErrorCallback(e, callback)
        }
    }

    private fun buildCacheKey(str: String): String {
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(str.toByteArray(charset("UTF-8")))
        val digest = messageDigest.digest()
        var sb = ""
        for (b in digest) {
            sb += String.format("%02x", b)
        }
        return sb
    }

    private fun buildCacheKey(url: URL): String = buildCacheKey(url.toString())

    private fun buildCacheDir(cacheKey: String): File = File(context.cacheDir.absolutePath + "/" + cacheKey + "/")

    private fun readAsBytes(inputStream: InputStream): ByteArray? {
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            val byteArray = ByteArray(2048)
            while (true) {
                val count = inputStream.read(byteArray, 0, 2048)
                if (count <= 0) {
                    break
                }
                else {
                    byteArrayOutputStream.write(byteArray, 0, count)
                }
            }
            return byteArrayOutputStream.toByteArray()
        }
    }

    private fun inflate(byteArray: ByteArray): ByteArray? {
        val inflater = Inflater()
        inflater.setInput(byteArray, 0, byteArray.size)
        val inflatedBytes = ByteArray(2048)
        ByteArrayOutputStream().use { inflatedOutputStream ->
            while (true) {
                val count = inflater.inflate(inflatedBytes, 0, 2048)
                if (count <= 0) {
                    break
                }
                else {
                    inflatedOutputStream.write(inflatedBytes, 0, count)
                }
            }
            inflater.end()
            return inflatedOutputStream.toByteArray()
        }
    }

    private fun unzip(inputStream: InputStream, cacheKey: String) {
        synchronized(fileLock) {
            val cacheDir = this.buildCacheDir(cacheKey)
            cacheDir.mkdirs()
            try {
                BufferedInputStream(inputStream).use {
                    ZipInputStream(it).use { zipInputStream ->
                        while (true) {
                            val zipItem = zipInputStream.nextEntry ?: break
                            if (zipItem.name.contains("/")) {
                                continue
                            }
                            val file = File(cacheDir, zipItem.name)
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
            } catch (e: Exception) {
                cacheDir.delete()
                throw e
            }
        }
    }
}
