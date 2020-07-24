package com.opensource.svgaplayer

import android.content.Context
import android.net.http.HttpResponseCache
import android.os.Handler
import android.util.Log
import com.opensource.svgaplayer.proto.MovieEntity
import com.opensource.svgaplayer.utils.log.LogUtils
import org.json.JSONObject
import java.io.*
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.Inflater
import java.util.zip.ZipInputStream

/**
 * Created by PonyCui 16/6/18.
 */

private var fileLock: Int = 0
private var isUnzipping = false

class SVGAParser(context: Context?) {
    private var mContextRef = WeakReference(context)

    @Volatile
    private var mFrameWidth: Int = 0

    @Volatile
    private var mFrameHeight: Int = 0

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
            threadPoolExecutor.execute {
                try {
                    LogUtils.info(TAG, "================ svga file download start ================")
                    if (HttpResponseCache.getInstalled() == null && !noCache) {
                        LogUtils.error(TAG, "SVGAParser can not handle cache before install HttpResponseCache. see https://github.com/yyued/SVGAPlayer-Android#cache")
                        LogUtils.error(TAG, "在配置 HttpResponseCache 前 SVGAParser 无法缓存. 查看 https://github.com/yyued/SVGAPlayer-Android#cache ")
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
                                        LogUtils.warn(TAG, "================ svga file download canceled ================")
                                        break
                                    }
                                    count = inputStream.read(buffer, 0, 4096)
                                    if (count == -1) {
                                        break
                                    }
                                    outputStream.write(buffer, 0, count)
                                }
                                if (cancelled) {
                                    LogUtils.warn(TAG, "================ svga file download canceled ================")
                                    return@execute
                                }
                                ByteArrayInputStream(outputStream.toByteArray()).use {
                                    LogUtils.info(TAG, "================ svga file download complete ================")
                                    complete(it)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    LogUtils.error(TAG, "================ svga file download fail ================")
                    LogUtils.error(TAG, "error: ${e.message}")
                    e.printStackTrace()
                    failure(e)
                }
            }
            return cancelBlock
        }
    }

    var fileDownloader = FileDownloader()

    companion object {
        private const val TAG = "SVGAParser"

        private val threadNum = AtomicInteger(0)
        private var mShareParser = SVGAParser(null)

        internal var threadPoolExecutor = Executors.newCachedThreadPool { r ->
             Thread(r, "SVGAParser-Thread-${threadNum.getAndIncrement()}")
        }

        fun setThreadPoolExecutor(executor: ThreadPoolExecutor) {
            threadPoolExecutor = executor
        }

        fun shareParser(): SVGAParser {
            return mShareParser
        }
    }

    fun init(context: Context) {
        mContextRef = WeakReference<Context?>(context)
    }

    fun setFrameSize(frameWidth: Int, frameHeight: Int) {
        mFrameWidth = frameWidth
        mFrameHeight = frameHeight
    }

    fun decodeFromAssets(name: String, callback: ParseCompletion?) {
        if (mContextRef.get() == null) {
            LogUtils.error(TAG, "在配置 SVGAParser context 前, 无法解析 SVGA 文件。")
        }
        try {
            LogUtils.info(TAG, "================ decode from assets ================")
            threadPoolExecutor.execute {
                mContextRef.get()?.assets?.open(name)?.let {
                    this.decodeFromInputStream(it, buildCacheKey("file:///assets/$name"), callback, true)
                }
            }
        } catch (e: java.lang.Exception) {
            this.invokeErrorCallback(e, callback)
        }
    }

    fun decodeFromURL(url: URL, callback: ParseCompletion?): (() -> Unit)? {
        LogUtils.info(TAG, "================ decode from url ================")
        if (this.isCached(buildCacheKey(url))) {
            LogUtils.info(TAG, "this url cached")
            threadPoolExecutor.execute {
                this.decodeFromCacheKey(buildCacheKey(url), callback)
            }
            return null
        }
        else {
            LogUtils.info(TAG, "no cached, prepare to download")
            return fileDownloader.resume(url, {
                this.decodeFromInputStream(it, this.buildCacheKey(url), callback)
            }, {
                this.invokeErrorCallback(it, callback)
            })
        }
    }

    fun decodeFromInputStream(inputStream: InputStream, cacheKey: String, callback: ParseCompletion?, closeInputStream: Boolean = false) {
        LogUtils.info(TAG, "================ decode from input stream ================")
        threadPoolExecutor.execute {
            try {
                readAsBytes(inputStream)?.let { bytes ->
                    if (bytes.size > 4 && bytes[0].toInt() == 80 && bytes[1].toInt() == 75 && bytes[2].toInt() == 3 && bytes[3].toInt() == 4) {
                        LogUtils.info(TAG, "decode from zip file")
                        if (!buildCacheDir(cacheKey).exists() || isUnzipping) {
                            synchronized(fileLock) {
                                if (!buildCacheDir(cacheKey).exists()) {
                                    isUnzipping = true
                                    LogUtils.info(TAG, "no cached, prepare to unzip")
                                    ByteArrayInputStream(bytes).use {
                                        unzip(it, cacheKey)
                                        isUnzipping = false
                                        LogUtils.info(TAG, "unzip success")
                                    }
                                }
                            }
                        }
                        this.decodeFromCacheKey(cacheKey, callback)
                    } else {
                        LogUtils.info(TAG, "decode from input stream, inflate start")
                        inflate(bytes)?.let {
                            val videoItem = SVGAVideoEntity(MovieEntity.ADAPTER.decode(it), File(cacheKey), mFrameWidth, mFrameHeight)
                            videoItem.prepare {
                                LogUtils.info(TAG, "decode from input stream, inflate end")
                                this.invokeCompleteCallback(videoItem, callback)
                            }
                        } ?: LogUtils.error(TAG, "inflate(bytes) cause exception")
                    }
                } ?: LogUtils.error(TAG, "readAsBytes(inputStream) cause exception")
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
    fun parse(assetsName: String, callback: ParseCompletion?) {
        this.decodeFromAssets(assetsName, callback)
    }

    /**
     * @deprecated from 2.4.0
     */
    @Deprecated("This method has been deprecated from 2.4.0.", ReplaceWith("this.decodeFromURL(url, callback)"))
    fun parse(url: URL, callback: ParseCompletion?) {
        this.decodeFromURL(url, callback)
    }

    /**
     * @deprecated from 2.4.0
     */
    @Deprecated("This method has been deprecated from 2.4.0.", ReplaceWith("this.decodeFromInputStream(inputStream, cacheKey, callback, closeInputStream)"))
    fun parse(inputStream: InputStream, cacheKey: String, callback: ParseCompletion?, closeInputStream: Boolean = false) {
        this.decodeFromInputStream(inputStream, cacheKey, callback, closeInputStream)
    }

    private fun invokeCompleteCallback(videoItem: SVGAVideoEntity, callback: ParseCompletion?) {
        if (mContextRef.get() == null) {
            Log.e("SVGAParser", "在配置 SVGAParser context 前, 无法解析 SVGA 文件。")
        }
        Handler(mContextRef.get()?.mainLooper).post {
            LogUtils.info(TAG, "================ parser complete ================")
            callback?.onComplete(videoItem)
        }
    }

    private fun invokeErrorCallback(e: java.lang.Exception, callback: ParseCompletion?) {
        e.printStackTrace()
        if (mContextRef.get() == null) {
            Log.e("SVGAParser", "在配置 SVGAParser context 前, 无法解析 SVGA 文件。")
        }
        LogUtils.error(TAG, "================ parser error ================")
        LogUtils.error(TAG, "error", e)
        Handler(mContextRef.get()?.mainLooper).post {
            callback?.onError()
        }
    }

    private fun isCached(cacheKey: String): Boolean {
        return buildCacheDir(cacheKey).exists()
    }

    private fun decodeFromCacheKey(cacheKey: String, callback: ParseCompletion?) {
        LogUtils.info(TAG, "================ decode from cache ================")
        LogUtils.debug(TAG, "decodeFromCacheKey called with cacheKey : $cacheKey")
        if (mContextRef.get() == null) {
            LogUtils.error(TAG, "在配置 SVGAParser context 前, 无法解析 SVGA 文件。")
        }
        try {
            val cacheDir = File(mContextRef.get()?.cacheDir?.absolutePath + "/" + cacheKey + "/")
            File(cacheDir, "movie.binary").takeIf { it.isFile }?.let { binaryFile ->
                try {
                    LogUtils.info(TAG, "binary change to entity")
                    FileInputStream(binaryFile).use {
                        LogUtils.info(TAG, "binary change to entity success")
                        this.invokeCompleteCallback(SVGAVideoEntity(MovieEntity.ADAPTER.decode(it), cacheDir, mFrameWidth, mFrameHeight), callback)
                    }
                } catch (e: Exception) {
                    LogUtils.error(TAG, "binary change to entity fail", e)
                    cacheDir.delete()
                    binaryFile.delete()
                    throw e
                }
            }
            File(cacheDir, "movie.spec").takeIf { it.isFile }?.let { jsonFile ->
                try {
                    LogUtils.info(TAG, "spec change to entity")
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
                                    LogUtils.info(TAG, "spec change to entity success")
                                    this.invokeCompleteCallback(SVGAVideoEntity(it, cacheDir, mFrameWidth, mFrameHeight), callback)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    LogUtils.error(TAG, "spec change to entity fail", e)
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

    private fun buildCacheDir(cacheKey: String): File = File(mContextRef.get()?.cacheDir?.absolutePath + "/" + cacheKey + "/")

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
                } else {
                    inflatedOutputStream.write(inflatedBytes, 0, count)
                }
            }
            inflater.end()
            return inflatedOutputStream.toByteArray()
        }
    }

    private fun unzip(inputStream: InputStream, cacheKey: String) {
        LogUtils.info(TAG, "================ unzip prepare ================")
        val cacheDir = this.buildCacheDir(cacheKey)
        cacheDir.mkdirs()
        try {
            BufferedInputStream(inputStream).use {
                ZipInputStream(it).use { zipInputStream ->
                    while (true) {
                        val zipItem = zipInputStream.nextEntry ?: break
                        if (zipItem.name.contains("../")) {
                            // 解压路径存在路径穿越问题，直接过滤
                            continue
                        }
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
                        LogUtils.error(TAG, "================ unzip complete ================")
                        zipInputStream.closeEntry()
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.error(TAG, "================ unzip error ================")
            LogUtils.error(TAG, "error", e)
            cacheDir.delete()
            throw e
        }
    }
}
