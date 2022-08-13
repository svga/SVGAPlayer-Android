package com.txl.ext_glide_test.ui

import android.animation.Animator
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.opensource.svgaplayer.drawer.SVGAAnimationDrawable
import com.txl.ext_glide_test.R
import com.txl.glide.model.SVGAModel
import kotlinx.android.synthetic.main.activity_load_asset_svga.*


/**
 * 加载Asset目录下的资源
 * */
class AddListenerActivity : AppCompatActivity() {

    private val imageString = "file:///android_asset/theme_award_beans.svga"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_asset_svga)
        Glide.with(this).load(SVGAModel(imageString,repeatCount = 0)).addListener(object :RequestListener<Drawable>{
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
                    resource.animatorListener = object :Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {
                            Log.d("AddListenerActivity","onAnimationStart")
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            Log.d("AddListenerActivity","onAnimationEnd")
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            Log.d("AddListenerActivity","onAnimationCancel")
                        }

                        override fun onAnimationRepeat(animation: Animator?) {
                            Log.d("AddListenerActivity","onAnimationRepeat")
                        }

                    }
                }
                return false
            }
        }).into(glideSVGAImg)
        loadBySvgaLib()
    }

    /**
     * SVGAImageView 只支持在xml中提前设置 图片路径 如果在代码中需要自自己创建解析对象
     * */
    private fun loadBySvgaLib() {
        val parse = SVGAParser(this)
        parse.decodeFromAssets("theme_award_beans.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                SVGAImageView.setImageDrawable(drawable)
                SVGAImageView.startAnimation()
            }

            override fun onError() {

            }
        })
    }
}