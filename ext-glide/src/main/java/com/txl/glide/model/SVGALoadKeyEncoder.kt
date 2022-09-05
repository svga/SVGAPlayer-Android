package com.txl.glide.model

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Encoder
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool
import com.opensource.svgaplayer.SVGACache
import com.opensource.svgaplayer.SVGAParser
import com.txl.glide.SVGALoadKey
import java.io.*
import java.net.URL

class SVGALoadKeyEncoder(private val context: Context) : Encoder<SVGALoadKey> {

    companion object {
        fun init(context: Context) {
            Glide.get(context).registry.append(SVGALoadKey::class.java, SVGALoadKeyEncoder(context))
        }


        fun intToBytes(value: Int): ByteArray? {
            val src = ByteArray(4)
            src[3] = (value shr 24 and 0xFF).toByte()
            src[2] = (value shr 16 and 0xFF).toByte()
            src[1] = (value shr 8 and 0xFF).toByte()
            src[0] = (value and 0xFF).toByte()
            return src
        }

        fun bytesToInt(src: ByteArray): Int {
            val value: Int
            val offset = 0
            value = (src[offset].toInt() and 0xFF
                    or (src[offset + 1].toInt() and 0xFF shl 8)
                    or (src[offset + 2].toInt() and 0xFF shl 16)
                    or (src[offset + 3].toInt() and 0xFF shl 24))
            return value
        }
    }

    private val byteArrayPool: ArrayPool = Glide.get(context).arrayPool

    /**
     * 存储 SVGA 数据
     * 1. 先存储 svgaModel 占用 字节长度 在存储SVGAModel 对象
     * 2. 存储音频文件路径占用字节长度 在储存 文件缓存路径
     * 3. 储存svga 文件流
     *
     * */
    override fun encode(svgaLoadKey: SVGALoadKey, file: File, options: Options): Boolean {
        val data = svgaLoadKey.inputStream ?: return false
        val buffer = byteArrayPool.get(ArrayPool.STANDARD_BUFFER_SIZE_BYTES,
            ByteArray::class.java)
        var success = false
        var os: OutputStream? = null
        var bos: ByteArrayOutputStream? = null
        var oos: ObjectOutputStream? = null
        try {
            os = FileOutputStream(file)
            var read: Int
            bos = ByteArrayOutputStream()
            oos = ObjectOutputStream(bos)
            oos.writeObject(svgaLoadKey.svgaModel)
            val byteArray = bos.toByteArray()
            //记录svgaLoadKey.svgaModel 的数组长度
            os.write(intToBytes(byteArray.size))
            os.write(byteArray)
            val cacheKey = when (svgaLoadKey.svgaModel.path) {
                is String -> {
                    SVGACache.buildCacheKey(svgaLoadKey.svgaModel.path)
                }
                is Uri -> {
                    SVGACache.buildCacheKey(svgaLoadKey.svgaModel.path.toString())
                }
                else -> {
                    svgaLoadKey.svgaModel.path.toString()
                }
            }
//            val audioPath = context.cacheDir.absolutePath+File.separator+cacheKey+File.separator+"audio"+File.separator
            val audioPath = cacheKey
            val audioPathArray = audioPath.toByteArray()
            os.write(intToBytes(audioPathArray.size))
            os.write(audioPathArray)

            while (data.read(buffer).also { read = it } != -1) {
                os.write(buffer, 0, read)
            }
            success = true
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            os?.close()
            bos?.close()
            oos?.close()
            byteArrayPool.put(buffer)
        }
        return success
    }
}