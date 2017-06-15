package com.opensource.svgaplayer

import android.app.Activity
import android.content.Context

import org.json.JSONObject

import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.logging.Handler
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Created by PonyCui_Home on 16/6/18.
 */
class SVGAParser(val context: Context) {

    interface ParseCompletion {

        fun onComplete(videoItem: SVGAVideoEntity)
        fun onError()

    }

    companion object {

        private var sharedLock: Int = 0

    }

    fun parse(assetsName: String): SVGAVideoEntity? {
        try {
            context.assets.open(assetsName)?.let {
                return parse(it, cacheKey("file:///assets/" + assetsName))
            }
        } catch (e: Exception) {}
        return null
    }

    fun parse(url: URL): SVGAVideoEntity? {
        try {
            if (cacheDir(cacheKey(url)).exists()) {
                return parse(null, cacheKey(url))
            }
            else {
                (url.openConnection() as? HttpURLConnection)?.let {
                    it.connectTimeout = 20 * 1000
                    it.requestMethod = "GET"
                    it.connect()
                    return parse(it.inputStream, cacheKey(url))
                }
            }
        } catch (e: Exception) {}
        return null
    }

    fun parse(url: URL, callback: ParseCompletion) {
        Thread(Runnable {
            parse(url)?.let {
                (context as? Activity)?.runOnUiThread {
                    callback.onComplete(it)
                }
                return@Runnable
            }
            (context as? Activity)?.runOnUiThread {
                callback.onError()
            }
        }).start()
    }

    fun parse(inputStream: InputStream?, cacheKey: String): SVGAVideoEntity? {
        inputStream?.let {
            if (!cacheDir(cacheKey).exists()) {
                unzip(it, cacheKey)
            }
        }
        val cacheDir = File(context.cacheDir.absolutePath + "/" + cacheKey + "/")
        val jsonFile = File(cacheDir, "movie.spec")
        try {
            FileInputStream(jsonFile)?.let {
                val fileInputStream = it
                val byteArrayOutputStream = ByteArrayOutputStream()
                val buffer = ByteArray(2048)
                while (true) {
                    val size = fileInputStream.read(buffer, 0, buffer.size)
                    if (size == -1) {
                        break
                    }
                    byteArrayOutputStream.write(buffer, 0, size)
                }
                byteArrayOutputStream.toString()?.let {
                    JSONObject(it)?.let {
                        fileInputStream.close()
                        return SVGAVideoEntity(it, cacheDir)
                    }
                }
            }
        } catch (e: Exception) {
            cacheDir.delete()
            jsonFile.delete()
        }
        return null
    }

    fun parse(inputStream: InputStream?, cacheKey: String, callback: ParseCompletion) {
        synchronized(sharedLock, {
            val videoItem = parse(inputStream, cacheKey)
            Thread({
                if (videoItem != null) {
                    callback.onComplete(videoItem)
                }
                else {
                    callback.onError()
                }
            }).start()
        })
    }

    private fun cacheKey(str: String): String {
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(str.toByteArray(charset("UTF-8")))
        val digest = messageDigest.digest()
        val sb = StringBuffer()
        for (b in digest) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    private fun cacheKey(url: URL): String {
        return cacheKey(url.toString())
    }

    private fun cacheDir(cacheKey: String): File {
        return File(context.cacheDir.absolutePath + "/" + cacheKey + "/")
    }

    private fun unzip(inputStream: InputStream, cacheKey: String) {
        try {
            val cacheDir = this.cacheDir(cacheKey)
            cacheDir.mkdirs()
            val zipInputStream = ZipInputStream(BufferedInputStream(inputStream))
            while (true) {
                val zipItem = zipInputStream.nextEntry ?: break
                val file = File(cacheDir, zipItem.name)
                val fileOutputStream = FileOutputStream(file)
                val buff = ByteArray(2048)
                while (true) {
                    val readBytes = zipInputStream.read(buff)
                    if (readBytes <= 0) {
                        break
                    }
                    fileOutputStream.write(buff, 0, readBytes)
                }
                fileOutputStream.close()
                zipInputStream.closeEntry()
            }
            zipInputStream.close()
        } catch (e: Exception) { }
    }

}