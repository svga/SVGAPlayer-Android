package com.opensource.svgaplayer.glideplugin

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.opensource.svgaplayer.SVGAVideoEntity
import com.opensource.svgaplayer.proto.MovieEntity
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

/**
 * Created by 张宇 on 2018/11/28.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
internal class SVGAEntityFileDecoder : ResourceDecoder<File, SVGAVideoEntity> {

    private val movieBinary = "movie.binary"
    private val movieSpec = "movie.spec"

    override fun handles(source: File, options: Options): Boolean =
        source.isDirectory && source.hasChild(movieBinary, movieSpec)

    override fun decode(source: File, width: Int, height: Int, options: Options): SVGAEntityResource? {
        try {
            File(source, movieBinary).takeIf { it.isFile }?.let { binaryFile ->
                try {
                    FileInputStream(binaryFile).use {
                        val entity = SVGAVideoEntity(MovieEntity.ADAPTER.decode(it), source)
                        return SVGAEntityResource(entity, source.totalSpace.toInt())
                    }
                } catch (e: Exception) {
                    binaryFile.delete()
                    throw e
                }
            }
            File(source, movieSpec).takeIf { it.isFile }?.let { jsonFile ->
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
                            val jsonObj = JSONObject(byteArrayOutputStream.toString())
                            val entity = SVGAVideoEntity(jsonObj, source)
                            return SVGAEntityResource(entity, source.totalSpace.toInt())
                        }
                    }
                } catch (e: Exception) {
                    jsonFile.delete()
                    throw e
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    private fun File.hasChild(vararg fileNames: String): Boolean {
        if (this.isDirectory) {
            val childFileNames = this.list().toSet()
            return fileNames.any { childFileNames.contains(it) }
        }
        return false
    }
}