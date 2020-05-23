package com.opensource.svgaplayer.task

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import android.util.LruCache
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.opensource.svgaplayer.proto.MovieEntity
import com.opensource.svgaplayer.utils.ResUtil
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.security.MessageDigest
import java.util.LinkedList
import java.util.concurrent.TimeUnit
import java.util.zip.Inflater
import java.util.zip.ZipInputStream

/**
 * parse 中心
 */
object DecodeParseCenter {

    private val TAG = DecodeParseCenter::class.java.simpleName

    private var fileLock: Int = 0

    private const val MSG_TASK_RUN_IF_NEED = 1

    private const val MSG_TASK_SUCCESS = 2
    private const val MSG_TASK_FAIL = 3

    private const val MSG_TASK_ADD_BY_URL = 4
    private const val MSG_TASK_ADD_BY_ASSETS = 5

    /**
     * 可同时运行的最大任务数量
     */
    private var mMaxTasks = 5

    /**
     * 最大的缓存数量
     */
    private var mMaxCache = 5

    /**
     * 等待队列
     */
    private val mWaitTasks = LinkedList<DecodeTask>()

    /**
     * 运行栈
     */
    private val mRunningTasks = mutableMapOf<String, DecodeTask>()

    private val mHandler: DecodeParseHandler by lazy {
        DecodeParseHandler()
    }

    private var mEntityCache: LruCache<String, SVGAVideoEntity>? = null

    private var mContext: Context? = null

    init {
        mEntityCache = object : LruCache<String, SVGAVideoEntity>(mMaxCache) {
        }
    }

    fun initCenter(context: Context) {
        mContext = context.applicationContext
    }

    /**
     * 创建 url 任务
     */
    fun addTask(url: URL, callback: SVGAParser.ParseCompletion?): String {
        val key = buildCacheKey(url.toString())

        val entity = getCache(key)
        entity?.let {
            callback?.onComplete(it)
            return key
        }

        val task = DecodeUrlTask(url.toString(), callback)
        task.taskCacheKey = key
        mHandler.sendMsg(MSG_TASK_ADD_BY_URL, task)

        return key
    }

    /**
     * 创建 assets 任务
     */
    fun addTask(assets: String, callback: SVGAParser.ParseCompletion?): String {

        val task = DecodeAssetsTask(assets, callback)
        val key = buildCacheKey(task.getPath())

        val entity = getCache(key)
        entity?.let {
            callback?.onComplete(it)
            return key
        }

        task.taskCacheKey = key
        mHandler.sendMsg(MSG_TASK_ADD_BY_ASSETS, task)

        return key
    }

    fun getFromAssets(path: String): InputStream {
        if (mContext == null) {
            throw IllegalArgumentException("please call initCenter first")
        }
        return ResUtil.getFromAssets(mContext!!, path)
    }

    fun decodeInputStream(task: DecodeTask, inputStream: InputStream, closeInputStream: Boolean) {
        try {
            readAsBytes(inputStream)?.let { bytes ->
                if (bytes.size > 4 && bytes[0].toInt() == 80 && bytes[1].toInt() == 75 && bytes[2].toInt() == 3 && bytes[3].toInt() == 4) {
                    if (!buildCacheDir(task.taskCacheKey).exists()) {
                        ByteArrayInputStream(bytes).use {
                            unzip(it, task.taskCacheKey)
                        }
                    }
                    decodeFromCacheKey(task.taskCacheKey)
                } else {
                    inflate(bytes)?.let {
                        val videoItem = SVGAVideoEntity(
                                MovieEntity.ADAPTER.decode(it),
                                File(task.taskCacheKey)
                        )
                        videoItem.prepare {
                            invokeCompleteCallback(task.taskCacheKey, videoItem)
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            invokeErrorCallback(task.taskCacheKey, e)
        } finally {
            if (closeInputStream) {
                inputStream.close()
            }
        }
    }

    fun decodeFromCacheKey(cacheKey: String) {
        if (mContext == null) {
            Log.e(TAG, "please call initCenter first")
        }
        try {
            val cacheDir = File(mContext?.cacheDir?.absolutePath + "/" + cacheKey + "/")
            File(cacheDir, "movie.binary").takeIf { it.isFile }?.let { binaryFile ->
                try {
                    FileInputStream(binaryFile).use {
                        invokeCompleteCallback(
                                cacheKey,
                                SVGAVideoEntity(MovieEntity.ADAPTER.decode(it), cacheDir)
                        )
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
                                JSONObject(it).let { json ->
                                    invokeCompleteCallback(
                                            cacheKey,
                                            SVGAVideoEntity(json, cacheDir)
                                    )
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
            invokeErrorCallback(cacheKey, e)
        }
    }

    /**
     * 从内存缓存中查找
     */
    fun getCache(taskCacheKey: String): SVGAVideoEntity? {
        return mEntityCache?.get(taskCacheKey)
    }

    /**
     * 是否拥有本地缓存
     */
    fun hasDiskCached(cacheKey: String): Boolean {
        return buildCacheDir(cacheKey).exists()
    }

    /**
     * 移除某个任务
     */
    fun removeTask(taskCacheKey: String) {
        var targetTask: DecodeTask? = null
        for (task in mWaitTasks) {
            if (taskCacheKey == task.taskCacheKey) {
                targetTask = task
            }
        }
        targetTask?.let {
            mWaitTasks.remove(it)
            return
        }
        mRunningTasks.remove(taskCacheKey)
    }

    /**
     * 清空所有任务
     */
    fun clearAllTask() {
        mWaitTasks.clear()
        mRunningTasks.clear()
        val pool = SVGAParser.getThreadPoolExecutor()
        try {
            pool.shutdown()
            if (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
                pool.shutdownNow()
            }
        } catch (e: Exception) {
            pool.shutdownNow()
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
        synchronized(fileLock) {
            val cacheDir = buildCacheDir(cacheKey)
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

    private fun buildCacheDir(cacheKey: String): File =
            File(mContext?.cacheDir?.absolutePath + "/" + cacheKey + "/")

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

    fun invokeCompleteCallback(taskCacheKey: String, entity: SVGAVideoEntity) {
        if (mContext == null) {
            Log.e(TAG, "please call initCenter first")
        }
        mHandler.sendMsg(MSG_TASK_SUCCESS, DecodeResult(taskCacheKey, entity = entity))
    }

    fun invokeErrorCallback(taskCacheKey: String, e: java.lang.Exception) {
        e.printStackTrace()
        if (mContext == null) {
            Log.e(TAG, "please call initCenter first")
        }
        mHandler.sendMsg(MSG_TASK_FAIL, DecodeResult(taskCacheKey, error = e))
    }

    private fun dealTaskUrl(task: DecodeUrlTask) {
        mWaitTasks.add(task)
        mHandler.sendMsg(MSG_TASK_RUN_IF_NEED)
    }

    private fun dealTaskAssets(task: DecodeAssetsTask) {
        mWaitTasks.add(task)
        mHandler.sendMsg(MSG_TASK_RUN_IF_NEED)
    }

    /**
     * 加载任务
     */
    private fun runTasks() {
        while (mRunningTasks.size < mMaxTasks) {
            val task = mWaitTasks.poll()
            task ?: break
            val entity = getCache(task.taskCacheKey)
            if (entity == null) {
                if (mRunningTasks[task.taskCacheKey] == null) {
                    mRunningTasks[task.taskCacheKey] = task
                    SVGAParser.getThreadPoolExecutor().execute(task)
                }
            } else {
                task.callback?.onComplete(entity)
            }
        }
    }

    private fun dealSuccess(result: DecodeResult) {
        val task = mRunningTasks.remove(result.taskKey)
        task?.callback?.let { callback ->
            result.entity?.let {
                callback.onComplete(it)
                mEntityCache?.put(result.taskKey, it)
            }
        }
        mHandler.sendMsg(MSG_TASK_RUN_IF_NEED)
    }

    private fun dealFail(result: DecodeResult) {
        val task = mRunningTasks.remove(result.taskKey)
        task?.callback?.onError()
        mHandler.sendMsg(MSG_TASK_RUN_IF_NEED)
    }

    private class DecodeParseHandler : Handler() {

        fun sendMsg(what: Int, target: Any? = null) {
            if (target == null) {
                sendEmptyMessage(what)
            } else {
                val msg = this.obtainMessage()
                msg.what = what
                msg.obj = target
                sendMessage(msg)
            }
        }

        override fun handleMessage(msg: Message?) {
            msg?.let { m ->
                when (m.what) {
                    MSG_TASK_ADD_BY_URL -> {
                        dealTaskUrl(m.obj as DecodeUrlTask)
                    }
                    MSG_TASK_ADD_BY_ASSETS -> {
                        dealTaskAssets(m.obj as DecodeAssetsTask)
                    }
                    MSG_TASK_RUN_IF_NEED -> {
                        runTasks()
                    }
                    MSG_TASK_SUCCESS -> {
                        dealSuccess(msg.obj as DecodeResult)
                    }
                    MSG_TASK_FAIL -> {
                        dealFail(msg.obj as DecodeResult)
                    }
                }
            }
            super.handleMessage(msg)
        }
    }
}