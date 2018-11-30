package com.opensource.svgaplayer.glideplugin

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.opensource.svgaplayer.SVGAVideoEntity
import com.opensource.svgaplayer.proto.MovieEntity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.zip.InflaterInputStream

/**
 * Created by 张宇 on 2018/11/26.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 *
 */
internal class SVGAEntityStreamDecoder(private val cachePath: String) :
    AbsSVGAEntityDecoder(), ResourceDecoder<InputStream, SVGAVideoEntity> {

    override fun handles(source: InputStream, options: Options): Boolean =
        readHeadAsBytes(source)?.isZipFormat == false

    override fun decode(source: InputStream, width: Int, height: Int, options: Options): SVGAEntityResource? {
        inflate(source)?.let { bytesOrigin ->
            val entity = SVGAVideoEntity(MovieEntity.ADAPTER.decode(bytesOrigin), File(cachePath))
            return SVGAEntityResource(entity, bytesOrigin.size)
        }
        return null
    }

    private fun inflate(source: InputStream): ByteArray? = attempt {
        InflaterInputStream(source).use { input ->
            val buffer = ByteArray(2048)
            ByteArrayOutputStream().use { output ->
                while (true) {
                    val cnt = input.read(buffer, 0, 2048)
                    if (cnt <= 0) break
                    output.write(buffer, 0, cnt)
                }
                output.toByteArray()
            }
        }
    }

}