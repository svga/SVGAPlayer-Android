package com.txl.glide.model

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.request.target.Target
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGASimpleParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.opensource.svgaplayer.drawer.SVGAAnimationDrawable
import com.txl.glide.model.SVGALoadKeyEncoder.Companion.bytesToInt
import com.txl.glide.resource.SVGADrawableResource
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.ObjectInputStream

class SVGAStreamDecoder(private val context: Context) :
    ResourceDecoder<InputStream, SVGAAnimationDrawable> {

    companion object {
        fun init(context: Context) {
            Glide.get(context).registry.append(
                Registry.BUCKET_ANIMATION,
                InputStream::class.java,
                SVGAAnimationDrawable::class.java,
                SVGAStreamDecoder(context)
            )
        }
    }

    private val svgaSimpleParser = SVGASimpleParser()
    override fun handles(source: InputStream, options: Options): Boolean {
        return true
    }

    override fun decode(
        source: InputStream,
        width: Int,
        height: Int,
        options: Options,
    ): Resource<SVGAAnimationDrawable>? {
        val fis = source
        val length = ByteArray(4)
        fis.read(length)
        val svgaModelSize = bytesToInt(length)
        val svgaModelArray = ByteArray(svgaModelSize)
        fis.read(svgaModelArray)
        val bis = ByteArrayInputStream(svgaModelArray)
        val ois = ObjectInputStream(bis)
        val svgaModel = ois.readObject() as SVGAModel

        //读取音频的缓存路径
        val audioPathLength = ByteArray(4)
        fis.read(audioPathLength)
        val audioPathSize = bytesToInt(audioPathLength)
        val audioPathArray = ByteArray(audioPathSize)
        fis.read(audioPathArray)
        val audioPath = String(audioPathArray)
        val requestedWidth = if (width == Target.SIZE_ORIGINAL) {
            0
        } else {
            width
        }
        val requestedHeight = if (height == Target.SIZE_ORIGINAL) {
            0
        } else {
            height
        }
        val svga: SVGAVideoEntity? = svgaSimpleParser.decodeFromInputStream(
            inputStream = source,
            audioPath,
            requestedWidth,
            requestedHeight
        )
        bis.close()
        ois.close()
        svga?.let {
            return SVGADrawableResource(
                SVGAAnimationDrawable(
                    it,
                    repeatCount = svgaModel.repeatCount,
                    repeatMode = svgaModel.repeatMode,
                    dynamicItem = SVGADynamicEntity()
                )
            )
        }
        return null

    }
}