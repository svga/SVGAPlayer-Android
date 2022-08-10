package com.txl.glide.model

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Encoder
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool
import com.txl.glide.SVGALoadKey
import java.io.*

class SVGALoadKeyEncoder( context: Context) : Encoder<SVGALoadKey> {

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