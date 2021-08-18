package com.opensource.svgaplayer

import android.content.Context
import com.opensource.svgaplayer.utils.log.LogUtils
import java.io.File
import java.net.URL
import java.security.MessageDigest

/**
 * SVGA 缓存管理
 */
object SVGACache {
    enum class Type {
        DEFAULT,
        FILE
    }

    private const val TAG = "SVGACache"
    private var type: Type = Type.DEFAULT
    private var cacheDir: String = "/"
        get() {
            if (field != "/") {
                val dir = File(field)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
            }
            return field
        }


    fun onCreate(context: Context?) {
        onCreate(context, Type.DEFAULT)
    }

    fun onCreate(context: Context?, type: Type) {
        if (isInitialized()) return
        context ?: return
        cacheDir = "${context.cacheDir.absolutePath}/svga/"
        File(cacheDir).takeIf { !it.exists() }?.mkdirs()
        this.type = type
    }

    /**
     * 清理缓存
     */
    fun clearCache() {
        if (!isInitialized()) {
            LogUtils.error(TAG, "SVGACache is not init!")
            return
        }
        SVGAParser.threadPoolExecutor.execute {
            clearDir(cacheDir)
            LogUtils.info(TAG, "Clear svga cache done!")
        }
    }

    // 清除目录下的所有文件
    internal fun clearDir(path: String) {
        try {
            val dir = File(path)
            dir.takeIf { it.exists() }?.let { parentDir ->
                parentDir.listFiles()?.forEach { file ->
                    if (!file.exists()) {
                        return@forEach
                    }
                    if (file.isDirectory) {
                        clearDir(file.absolutePath)
                    }
                    file.delete()
                }
            }
        } catch (e: Exception) {
            LogUtils.error(TAG, "Clear svga cache path: $path fail", e)
        }
    }

    fun isInitialized(): Boolean {
        return "/" != cacheDir && File(cacheDir).exists()
    }

    fun isDefaultCache(): Boolean = type == Type.DEFAULT

    fun isCached(cacheKey: String): Boolean {
        return if (isDefaultCache()) {
            buildCacheDir(cacheKey)
        } else {
            buildSvgaFile(
                    cacheKey
            )
        }.exists()
    }

    fun buildCacheKey(str: String): String {
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(str.toByteArray(charset("UTF-8")))
        val digest = messageDigest.digest()
        var sb = ""
        for (b in digest) {
            sb += String.format("%02x", b)
        }
        return sb
    }

    fun buildCacheKey(url: URL): String = buildCacheKey(url.toString())

    fun buildCacheDir(cacheKey: String): File {
        return File("$cacheDir$cacheKey/")
    }

    fun buildSvgaFile(cacheKey: String): File {
        return File("$cacheDir$cacheKey.svga")
    }

    fun buildAudioFile(audio: String): File {
        return File("$cacheDir$audio.mp3")
    }

}