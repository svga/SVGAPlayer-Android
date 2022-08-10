package com.txl.ext_glide_test

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGASimpleParser
import com.opensource.svgaplayer.drawer.SVGAAnimationDrawable
import com.txl.glide.SVGALoadKey
import com.txl.glide.model.SVGALoader
import com.txl.glide.StreamSVGADecoder
import com.txl.glide.model.SVGALoadType
import com.txl.glide.model.SVGAModel

class SvgaDemoActivity : AppCompatActivity() {
    private val  tag = SvgaDemoActivity::class.java.simpleName
    private lateinit var svgaImage:ImageView
    private lateinit var svgaImagePlayer:SVGAImageView
    private lateinit var changeScaleType:TextView

    private var scaleTypeIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_svga_demo)
        val svgaParser = SVGAParser(applicationContext)
        SVGALoader.init(this)
        Glide.get(this).registry.append(Registry.BUCKET_BITMAP_DRAWABLE, SVGALoadKey::class.java, SVGAAnimationDrawable::class.java, StreamSVGADecoder(
            SVGASimpleParser(),this))
//        Glide.with(this).setDefaultRequestOptions()
        initView()
    }

    private fun initView() {
        svgaImage = findViewById(R.id.svgaImage)
        svgaImagePlayer = findViewById(R.id.svgaImagePlayer)
        changeScaleType = findViewById(R.id.tvChangeScaleType)
        val imageString = "file:///android_asset/theme_award_beans.svga"
        val uri = Uri.parse(imageString)
        svgaImage.setOnClickListener {
//          Glide.with(this).`as`(SVGAAnimationDrawable::class.java).load(uri).addListener(object :RequestListener<SVGAAnimationDrawable>{
//              override fun onLoadFailed(
//                  e: GlideException?,
//                  model: Any?,
//                  target: Target<SVGAAnimationDrawable>?,
//                  isFirstResource: Boolean,
//              ): Boolean {
//                  Log.e(SVGAAnimationDrawable.TAG,"onLoadFailed")
//                  return false
//              }
//
//              override fun onResourceReady(
//                  resource: SVGAAnimationDrawable?,
//                  model: Any?,
//                  target: Target<SVGAAnimationDrawable>?,
//                  dataSource: DataSource?,
//                  isFirstResource: Boolean,
//              ): Boolean {
////                  svgaImagePlayer.setImageDrawable(resource?.svgaDrawable)
////                  svgaImagePlayer.stepToFrame(0,true)
//                  Log.e(SVGAAnimationDrawable.TAG,"onResourceReady")
//                  return false
//              }
//
//          })/*.transform(SVGAAnimationDrawable::class.java,SVGADrawableTransformation(CenterCrop()))*/.into(svgaImage)

//            Glide.with(this).load(uri).into(svgaImage)
//            Glide.with(this).load(imageString).into(svgaImage)

//            Glide.with(this).load(SVGAModel(imageString,SVGALoadType.String)).into(svgaImage)
//            Glide.with(this).load(SVGAModel(uri,SVGALoadType.Uri)).into(svgaImage)
            Glide.with(this).load(SVGAModel("https://jojostorage.tinman.cn/app/forestAppResource/dripCollectDynamic.svga",SVGALoadType.String))/*.diskCacheStrategy(
                DiskCacheStrategy.NONE)*/.into(svgaImage)
        }
        changeScaleType.setOnClickListener {
            scaleTypeIndex++
            svgaImagePlayer.scaleType = getScaleType(scaleTypeIndex)
            svgaImage.scaleType = getScaleType(scaleTypeIndex)
            changeScaleType.text = "切换缩放模式 当前：${svgaImage.scaleType}"
        }
    }

    fun getScaleType(index:Int):ImageView.ScaleType{
        when(index%7){
            0->{
                return ImageView.ScaleType.MATRIX
            }
            1->{
                return ImageView.ScaleType.FIT_XY
            }
            2->{
                return ImageView.ScaleType.FIT_START
            }
            3->{
                return ImageView.ScaleType.FIT_CENTER
            }
            4->{
                return ImageView.ScaleType.FIT_END
            }
            5->{
                return ImageView.ScaleType.CENTER
            }
            6->{
                return ImageView.ScaleType.CENTER_CROP
            }
            7->{
                return ImageView.ScaleType.CENTER_INSIDE
            }
        }
        return ImageView.ScaleType.CENTER_CROP
    }
}