package com.opensource.svgaplayer

import android.content.Context
import android.net.http.HttpResponseCache
import android.os.Handler
import android.os.Looper
import com.opensource.svgaplayer.proto.MovieEntity
import com.opensource.svgaplayer.utils.log.LogUtils
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
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
    private var mContext = context?.applicationContext

    init {
        SVGACache.onCreate(context)
    }

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

        open fun resume(
            url: URL,
            complete: (inputStream: InputStream) -> Unit,
            failure: (e: Exception) -> Unit
        ): () -> Unit {
            var cancelled = false
            val cancelBlock = {
                cancelled = true
            }
            threadPoolExecutor.execute {
                try {
                    LogUtils.info(
                        TAG,
                        "================ svga file: $url download start ================"
                    )
                    if (HttpResponseCache.getInstalled() == null && !noCache) {
                        LogUtils.error(
                            TAG,
                            "SVGAParser can not handle cache before install HttpResponseCache. see https://github.com/yyued/SVGAPlayer-Android#cache"
                        )
                        LogUtils.error(
                            TAG,
                            "在配置 HttpResponseCache 前 SVGAParser 无法缓存. 查看 https://github.com/yyued/SVGAPlayer-Android#cache "
                        )
                    }
                    (url.openConnection() as? HttpURLConnection)?.let {
                        it.connectTimeout = 20 * 1000
                        it.requestMethod = "GET"
                        it.setRequestProperty("Connection", "close")
                        it.connect()
                        it.inputStream.use { inputStream ->
                            ByteArrayOutputStream().use { outputStream ->
                                val buffer = ByteArray(4096)
                                var count: Int
                                while (true) {
                                    if (cancelled) {
                                        LogUtils.warn(
                                            TAG,
                                            "================ svga file: $url download canceled ================"
                                        )
                                        break
                                    }
                                    count = inputStream.read(buffer, 0, 4096)
                                    if (count == -1) {
                                        break
                                    }
                                    outputStream.write(buffer, 0, count)
                                }
                                if (cancelled) {
                                    LogUtils.warn(
                                        TAG,
                                        "================ svga file: $url download canceled ================"
                                    )
                                    return@execute
                                }
                                ByteArrayInputStream(outputStream.toByteArray()).use {
                                    LogUtils.info(
                                        TAG,
                                        "================ svga file: $url download complete ================"
                                    )
                                    complete(it)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
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
        mContext = context.applicationContext
        SVGACache.onCreate(mContext)
    }

    fun setFrameSize(frameWidth: Int, frameHeight: Int) {
        mFrameWidth = frameWidth
        mFrameHeight = frameHeight
    }

    fun decodeFromAssets(name: String, callback: ParseCompletion?) {
        if (mContext == null) {
            LogUtils.error(TAG, "在配置 SVGAParser context 前, 无法解析 SVGA 文件。")
            return
        }
        LogUtils.info(TAG, "================ decode $name from assets ================")
        threadPoolExecutor.execute {
            try {
                mContext?.assets?.open(name)?.let {
                    this.decodeFromInputStream(
                        it,
                        SVGACache.buildCacheKey("file:///assets/$name"),
                        callback,
                        true,
                        alias = name
                    )
                }
            } catch (e: java.lang.Exception) {
                this.invokeErrorCallback(e, callback, name)
            }
        }
    }

    fun decodeFromURL(url: URL, callback: ParseCompletion?): (() -> Unit)? {
        if (mContext == null) {
            LogUtils.error(TAG, "在配置 SVGAParser context 前, 无法解析 SVGA 文件。")
            return null
        }
        val urlPath = url.toString()
        LogUtils.info(TAG, "================ decode $urlPath from url ================")
        val cacheKey = SVGACache.buildCacheKey(url);
        return if (SVGACache.isCached(cacheKey)) {
            LogUtils.info(TAG, "this url cached")
            threadPoolExecutor.execute {
                if (SVGACache.isDefaultCache()) {
                    this.decodeFromCacheKey(cacheKey, callback, alias = urlPath)
                } else {
                    this._decodeFromCacheKey(cacheKey, callback, alias = urlPath)
                }
            }
            return null
        } else {
            LogUtils.info(TAG, "no cached, prepare to download")
            fileDownloader.resume(url, {
                if (SVGACache.isDefaultCache()) {
                    this.decodeFromInputStream(it, cacheKey, callback, alias = urlPath)
                } else {
                    this._decodeFromInputStream(it, cacheKey, callback, alias = urlPath)
                }
            }, {
                LogUtils.error(
                    TAG,
                    "================ svga file: $url download fail ================"
                )
                this.invokeErrorCallback(it, callback, alias = urlPath)
            })
        }
    }

    /**
     * 读取解析本地缓存的svga文件.
     */
    fun _decodeFromCacheKey(cacheKey: String, callback: ParseCompletion?, alias: String) {
        val svga = SVGACache.buildSvgaFile(cacheKey)
        try {
            LogUtils.info(TAG, "$alias cache.binary change to entity")
            FileInputStream(svga).use { inputStream ->
                try {
                    readAsBytes(inputStream)?.let { bytes ->
                        LogUtils.info(TAG, "cache.inflate start")
                        inflate(bytes)?.let { inflateBytes ->
                            LogUtils.info(TAG, "cache.inflate success")
                            val videoItem = SVGAVideoEntity(
                                MovieEntity.ADAPTER.decode(inflateBytes),
                                File(cacheKey),
                                mFrameWidth,
                                mFrameHeight
                            )
                            videoItem.prepare {
                                LogUtils.info(TAG, "cache.prepare success")
                                this.invokeCompleteCallback(videoItem, callback, alias)
                            }
                        } ?: doError("cache.inflate(bytes) cause exception", callback, alias)
                    } ?: doError("cache.readAsBytes(inputStream) cause exception", callback, alias)
                } catch (e: Exception) {
                    this.invokeErrorCallback(e, callback, alias)
                } finally {
                    inputStream.close()
                }
            }
        } catch (e: Exception) {
            LogUtils.error(TAG, "$alias cache.binary change to entity fail", e)
            svga.takeIf { it.exists() }?.delete()
            this.invokeErrorCallback(e, callback, alias)
        }
    }

    fun doError(error: String, callback: ParseCompletion?, alias: String) {
        LogUtils.error(TAG, error)
        this.invokeErrorCallback(
            Exception(error),
            callback,
            alias
        )
    }

    /**
     * 读取解析来自URL的svga文件.并缓存成本地文件
     */
    fun _decodeFromInputStream(
        inputStream: InputStream,
        cacheKey: String,
        callback: ParseCompletion?,
        alias: String
    ) {
        threadPoolExecutor.execute {
            try {
                LogUtils.info(TAG, "$alias input.binary change to entity")
                readAsBytes(inputStream)?.let { bytes ->
                    threadPoolExecutor.execute {
                        SVGACache.buildSvgaFile(cacheKey).let { cacheFile ->
                            try {
                                cacheFile.takeIf { !it.exists() }?.createNewFile()
                                FileOutputStream(cacheFile).write(bytes)
                            } catch (e: Exception) {
                                LogUtils.error(TAG, "create cache file fail.", e)
                                cacheFile.delete()
                            }
                        }
                    }
                    LogUtils.info(TAG, "input.inflate start")
                    inflate(bytes)?.let { inflateBytes ->
                        LogUtils.info(TAG, "Input.inflate success")
                        val videoItem = SVGAVideoEntity(
                            MovieEntity.ADAPTER.decode(inflateBytes),
                            File(cacheKey),
                            mFrameWidth,
                            mFrameHeight
                        )
                        // 里面soundPool如果解析时load同一个svga的声音文件会出现无回调的情况,导致这里的callback不执行,
                        // 原因暂时未知.目前解决方案是公开imageview,drawable,entity的clear(),然后在播放带声音
                        // 的svgaimageview处,把解析完的drawable或者entity缓存下来,下次直接播放.用完再调用clear()
                        // 在ImageView添加clearsAfterDetached,用于控制imageview在onDetach的时候是否要自动调用clear.
                        // 以暂时缓解需要为RecyclerView缓存drawable或者entity的人士.用完记得调用clear()
                        videoItem.prepare {
                            LogUtils.info(TAG, "input.prepare success")
                            this.invokeCompleteCallback(videoItem, callback, alias)
                        }
                    } ?: doError("input.inflate(bytes) cause exception", callback, alias)
                } ?: doError("input.readAsBytes(inputStream) cause exception", callback, alias)
            } catch (e: Exception) {
                this.invokeErrorCallback(e, callback, alias)
            } finally {
                inputStream.close()
            }
        }
    }

    fun decodeFromInputStream(
        inputStream: InputStream,
        cacheKey: String,
        callback: ParseCompletion?,
        closeInputStream: Boolean = false,
        alias: String
    ) {
        if (mContext == null) {
            LogUtils.error(TAG, "在配置 SVGAParser context 前, 无法解析 SVGA 文件。")
            return
        }
        LogUtils.info(TAG, "================ decode $alias from input stream ================")
        threadPoolExecutor.execute {
            try {
                readAsBytes(inputStream)?.let { bytes ->
                    if (bytes.size > 4 && bytes[0].toInt() == 80 && bytes[1].toInt() == 75 && bytes[2].toInt() == 3 && bytes[3].toInt() == 4) {
                        LogUtils.info(TAG, "decode from zip file")
                        if (!SVGACache.buildCacheDir(cacheKey).exists() || isUnzipping) {
                            synchronized(fileLock) {
                                if (!SVGACache.buildCacheDir(cacheKey).exists()) {
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
                        this.decodeFromCacheKey(cacheKey, callback, alias)
                    } else {
                        LogUtils.info(TAG, "decode from input stream, inflate start")
                        inflate(bytes)?.let {
                            val videoItem = SVGAVideoEntity(
                                MovieEntity.ADAPTER.decode(it),
                                File(cacheKey),
                                mFrameWidth,
                                mFrameHeight
                            )
                            videoItem.prepare {
                                LogUtils.info(TAG, "decode from input stream, inflate end")
                                this.invokeCompleteCallback(videoItem, callback, alias)
                            }

                        } ?: this.invokeErrorCallback(
                            Exception("inflate(bytes) cause exception"),
                            callback,
                            alias
                        )
                    }
                } ?: this.invokeErrorCallback(
                    Exception("readAsBytes(inputStream) cause exception"),
                    callback,
                    alias
                )
            } catch (e: java.lang.Exception) {
                this.invokeErrorCallback(e, callback, alias)
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
    @Deprecated(
        "This method has been deprecated from 2.4.0.",
        ReplaceWith("this.decodeFromAssets(assetsName, callback)")
    )
    fun parse(assetsName: String, callback: ParseCompletion?) {
        this.decodeFromAssets(assetsName, callback)
    }

    /**
     * @deprecated from 2.4.0
     */
    @Deprecated(
        "This method has been deprecated from 2.4.0.",
        ReplaceWith("this.decodeFromURL(url, callback)")
    )
    fun parse(url: URL, callback: ParseCompletion?) {
        this.decodeFromURL(url, callback)
    }

    /**
     * @deprecated from 2.4.0
     */
    @Deprecated(
        "This method has been deprecated from 2.4.0.",
        ReplaceWith("this.decodeFromInputStream(inputStream, cacheKey, callback, closeInputStream)")
    )
    fun parse(
        inputStream: InputStream,
        cacheKey: String,
        callback: ParseCompletion?,
        closeInputStream: Boolean = false
    ) {
        this.decodeFromInputStream(inputStream, cacheKey, callback, closeInputStream, "")
    }

    private fun invokeCompleteCallback(
        videoItem: SVGAVideoEntity,
        callback: ParseCompletion?,
        alias: String
    ) {
        Handler(Looper.getMainLooper()).post {
            LogUtils.info(TAG, "================ $alias parser complete ================")
            callback?.onComplete(videoItem)
        }
    }

    private fun invokeErrorCallback(
        e: java.lang.Exception,
        callback: ParseCompletion?,
        alias: String
    ) {
        e.printStackTrace()
        LogUtils.error(TAG, "================ $alias parser error ================")
        LogUtils.error(TAG, "$alias parser error", e)
        Handler(Looper.getMainLooper()).post {
            callback?.onError()
        }
    }

    private fun decodeFromCacheKey(cacheKey: String, callback: ParseCompletion?, alias: String) {
        LogUtils.info(TAG, "================ decode $alias from cache ================")
        LogUtils.debug(TAG, "decodeFromCacheKey called with cacheKey : $cacheKey")
        if (mContext == null) {
            LogUtils.error(TAG, "在配置 SVGAParser context 前, 无法解析 SVGA 文件。")
            return
        }
        try {
            val cacheDir = SVGACache.buildCacheDir(cacheKey)
            File(cacheDir, "movie.binary").takeIf { it.isFile }?.let { binaryFile ->
                try {
                    LogUtils.info(TAG, "binary change to entity")
                    FileInputStream(binaryFile).use {
                        LogUtils.info(TAG, "binary change to entity success")
                        this.invokeCompleteCallback(
                            SVGAVideoEntity(
                                MovieEntity.ADAPTER.decode(it),
                                cacheDir,
                                mFrameWidth,
                                mFrameHeight
                            ),
                            callback,
                            alias
                        )
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
                                    this.invokeCompleteCallback(
                                        SVGAVideoEntity(
                                            it,
                                            cacheDir,
                                            mFrameWidth,
                                            mFrameHeight
                                        ),
                                        callback,
                                        alias
                                    )
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
            this.invokeErrorCallback(e, callback, alias)
        }
    }

    private fun readAsBytes(inputStream: InputStream): ByteArray? {
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            val byteArray = ByteArray(2048)
            while (true) {
                val count = inputStream.read(byteArray, 0, 2048)
                if (count <= 0) {
                    break
                } else {
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
        val cacheDir = SVGACache.buildCacheDir(cacheKey)
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
                        LogUtils.info(TAG, "================ unzip complete ================")
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
