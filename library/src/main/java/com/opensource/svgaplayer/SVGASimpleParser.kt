package com.opensource.svgaplayer

import com.opensource.svgaplayer.proto.MovieEntity
import com.opensource.svgaplayer.utils.log.LogUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.zip.Inflater

class SVGASimpleParser {
    fun decodeFromInputStream(
        inputStream: InputStream,
        audioPath: String,
        frameWidth: Int,
        frameHeight: Int,
        closeInputStream: Boolean = false
    ): SVGAVideoEntity? {
        val file = File(audioPath)
        if(!file.exists()){
            file.takeIf { !it.exists() }?.mkdirs()
        }
        var videoItem: SVGAVideoEntity? = null
        readAsBytes(inputStream)?.let { bytes ->
            inflate(bytes).let {
                videoItem = SVGAVideoEntity(
                    MovieEntity.ADAPTER.decode(it),
                    //这个直接这样给应该不行？
                    File(audioPath),
                    frameWidth,
                    frameHeight
                )
                videoItem?.prepare({}, null)
            }
        }
        return videoItem
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
}