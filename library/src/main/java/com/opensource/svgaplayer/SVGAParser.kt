package com.opensource.svgaplayer

import android.app.Activity
import android.content.Context

import org.json.JSONObject
import java.io.*

import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.zip.Inflater
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

    fun parse(assetsName: String, callback: ParseCompletion) {
        try {
            context.assets.open(assetsName)?.let {
                parse(it, cacheKey("file:///assets/" + assetsName), callback)
            }
        } catch (e: Exception) {}
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
        Thread({
            val BUFFER_SIZE = 4096
            try {
                if (cacheDir(cacheKey(url)).exists()) {
                    parse(null, cacheKey(url), callback)
                }
                else {
                    (url.openConnection() as? HttpURLConnection)?.let {
                        it.connectTimeout = 20 * 1000
                        it.requestMethod = "GET"
                        it.connect()
                        val inputStream = it.inputStream
                        val outputStream = ByteArrayOutputStream()
                        val buffer = ByteArray(BUFFER_SIZE)
                        var count: Int
                        while (true) {
                            count = inputStream.read(buffer, 0, BUFFER_SIZE)
                            if (count == -1) {
                                break
                            }
                            outputStream.write(buffer, 0, count)
                        }
                        parse(ByteArrayInputStream(outputStream.toByteArray()), cacheKey(url), callback)
                    }
                }
            } catch (e: Exception) {
                print(e)
            }
        }).start()
    }

    fun parse(inputStream: InputStream?, cacheKey: String): SVGAVideoEntity? {
        readAsBytes(inputStream)?.let {
            if (it.size > 4 && it[0].toInt() == 80 && it[1].toInt() == 75 && it[2].toInt() == 3 && it[3].toInt() == 4) {
                if (!cacheDir(cacheKey).exists()) {
                    try {
                        unzip(ByteArrayInputStream(it), cacheKey)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return null
                    }
                } else { }
            }
            else {
                try {
                    inflate(it)?.let {
                        return SVGAVideoEntity(Svga.MovieEntity.parseFrom(it), File(cacheKey))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }
        }
        val cacheDir = File(context.cacheDir.absolutePath + "/" + cacheKey + "/")
        val binaryFile = File(cacheDir, "movie.binary")
        val jsonFile = File(cacheDir, "movie.spec")
        if (binaryFile.isFile) {
            try {
                FileInputStream(binaryFile)?.let {
                    val videoItem = SVGAVideoEntity(Svga.MovieEntity.parseFrom(it), cacheDir)
                    it.close()
                    return videoItem
                }
            } catch (e: Exception) {
                e.printStackTrace()
                cacheDir.delete()
                binaryFile.delete()
                jsonFile.delete()
            }
        }
        else {
            try {
                FileInputStream(jsonFile)?.let { fileInputStream ->
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
                e.printStackTrace()
                cacheDir.delete()
                binaryFile.delete()
                jsonFile.delete()
            }
        }
        return null
    }

    fun parse(inputStream: InputStream?, cacheKey: String, callback: ParseCompletion) {
        Thread({
            synchronized(sharedLock, {
                val videoItem = parse(inputStream, cacheKey)
                Thread({
                    if (videoItem != null) {
                        if (context as? Activity != null) {
                            (context as? Activity)?.runOnUiThread {
                                callback.onComplete(videoItem)
                            }
                        }
                        else {
                            callback.onComplete(videoItem)
                        }
                    }
                    else {
                        if (context as? Activity != null) {
                            (context as? Activity)?.runOnUiThread {
                                callback.onError()
                            }
                        }
                        else {
                            callback.onError()
                        }
                    }
                }).start()
            })
        }).start()
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

    private fun readAsBytes(inputStream: InputStream?): ByteArray? {
        inputStream?.let {
            val byteArrayOutputStream = ByteArrayOutputStream()
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
        return null
    }

    private fun inflate(byteArray: ByteArray): ByteArray? {
        try {
            val inflater = Inflater()
            inflater.setInput(byteArray, 0, byteArray.size)
            val inflatedBytes = ByteArray(2048)
            val inflatedOutputStream = ByteArrayOutputStream()
            while (true) {
                val count = inflater.inflate(inflatedBytes, 0, 2048)
                if (count <= 0) {
                    break
                }
                else {
                    inflatedOutputStream.write(inflatedBytes, 0, count)
                }
            }
            return inflatedOutputStream.toByteArray()
        } catch (e: Exception) { e.printStackTrace(); }
        return null
    }

    private fun unzip(inputStream: InputStream, cacheKey: String) {
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
    }

}