package com.opensource.svgaplayer

import android.content.Context
import java.io.File
import java.net.URL
import java.security.MessageDigest


object SVGACache {
    enum class Type {
        DEFAULT,
        FILE
    }

    private var type: Type = Type.DEFAULT
    private var cacheDir: String = "/"

    fun onCreate(context: Context?) {
        onCreate(context, Type.DEFAULT)
    }

    fun onCreate(context: Context?, type: Type) {
        if (isInitialized()) return
        context ?: return
        cacheDir = "${context.cacheDir.absolutePath}/svga/"
        File(cacheDir).takeIf { !it.exists() }?.mkdir()
        this.type = type
    }

//    fun clearCache(context: Context?){
//        context ?: return
//        cacheDir = "${context.cacheDir.absolutePath}/svga/"
//        File(cacheDir).takeIf { it.exists() }?.delete()
//    }

    fun isInitialized(): Boolean {
        return "/" != cacheDir
    }

    fun isDefaultCache(): Boolean = type == Type.DEFAULT

    fun isCached(cacheKey: String): Boolean {
        return (if (isDefaultCache()) buildCacheDir(cacheKey) else buildSvgaFile(
                cacheKey
        )).exists()
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