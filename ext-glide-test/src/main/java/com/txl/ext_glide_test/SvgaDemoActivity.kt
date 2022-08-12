package com.txl.ext_glide_test

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.TextPaint
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGASimpleParser
import com.opensource.svgaplayer.drawer.SVGAAnimationDrawable
import com.txl.glide.SVGALoadKey
import com.txl.glide.StreamSVGADecoder
import com.txl.glide.model.SVGALoadType
import com.txl.glide.model.SVGALoader
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

            val dynamicEntity = SVGADynamicEntity()
            val textPaint = TextPaint()
            textPaint.color = Color.WHITE //字体颜色

            textPaint.textSize = 24f //字体大小

            textPaint.setShadowLayer(3f, 2f, 2f, -0x1000000) //字体阴影，不需要可以不用设置

            dynamicEntity.setDynamicText("30",
                textPaint,
                "text_day")
            Glide.with(this).load(SVGAModel(imageString,
                SVGALoadType.String,
                repeatMode = ValueAnimator.RESTART,
                repeatCount = 0)
            ).addListener(object :RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    if(resource is SVGAAnimationDrawable){

                        resource.animatorListener = object : Animator.AnimatorListener{
                            override fun onAnimationStart(animation: Animator?) {
                                Log.d(tag,"onAnimationStart")
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                Log.d(tag,"onAnimationEnd")
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                                Log.d(tag,"onAnimationCancel")
                            }

                            override fun onAnimationRepeat(animation: Animator?) {
                                Log.d(tag,"onAnimationRepeat")
                            }

                        }
                        resource.resetDynamicEntity(dynamicEntity)
                    }
                    return false
                }
            }).into(svgaImage)
//            Glide.with(this).load(SVGAModel(uri,SVGALoadType.Uri)).into(svgaImage)
//            Glide.with(this).load(SVGAModel("https://jojostorage.tinman.cn/app/forestAppResource/dripCollectDynamic.svga",SVGALoadType.String))/*.diskCacheStrategy(
//                DiskCacheStrategy.NONE)*/.into(svgaImage)
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