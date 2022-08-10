package com.txl.glide.model

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool
import com.opensource.svgaplayer.SVGASimpleParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.opensource.svgaplayer.drawer.SVGAAnimationDrawable
import com.txl.glide.SVGALoadKey
import com.txl.glide.model.SVGALoadKeyEncoder.Companion.bytesToInt
import com.txl.glide.resource.SVGADrawableResource
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.nio.ByteBuffer

class SVGALoadKeyDecoder(private val context: Context): ResourceDecoder<InputStream, SVGAAnimationDrawable> {

    companion object{
        fun init(context:Context){
            Glide.get(context).registry.append(
                Registry.BUCKET_ANIMATION, InputStream::class.java, SVGAAnimationDrawable::class.java, SVGALoadKeyDecoder(context))
        }
    }

    private val  svgaSimpleParser = SVGASimpleParser()
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
            val svgaModel = ois.readObject()
        var svga: SVGAVideoEntity? = svgaSimpleParser.decodeFromInputStream(inputStream = source)
        bis.close()
        ois.close()
        svga?.let {
            return SVGADrawableResource(SVGAAnimationDrawable(it))
        }
        return null

    }
}