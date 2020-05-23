package com.opensource.svgaplayer.utils

import android.net.http.HttpResponseCache
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object FileDownloader {


    fun resume(
            url: URL,
            complete: (inputStream: InputStream) -> Unit,
            failure: (e: Exception) -> Unit
    ): () -> Unit {
        var cancelled = false
        val cancelBlock = {
            cancelled = true
        }
        try {
            if (HttpResponseCache.getInstalled() == null) {
                Log.e(
                        "FileDownloader",
                        "FileDownloader can not handle cache before install HttpResponseCache. see https://github.com/yyued/SVGAPlayer-Android#cache"
                )
                Log.e(
                        "FileDownloader",
                        "在配置 HttpResponseCache 前 FileDownloader 无法缓存. 查看 https://github.com/yyued/SVGAPlayer-Android#cache "
                )
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
                            return cancelBlock
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
        return cancelBlock
    }
}