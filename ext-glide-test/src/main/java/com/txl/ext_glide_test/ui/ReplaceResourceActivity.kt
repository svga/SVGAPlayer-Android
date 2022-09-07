package com.txl.ext_glide_test.ui

import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextPaint
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.opensource.svgaplayer.drawer.SVGAAnimationDrawable
import com.txl.ext_glide_test.R
import kotlinx.android.synthetic.main.activity_load_asset_svga.*

/**
 * 替换SVGA资源
 * */
class ReplaceResourceActivity : AppCompatActivity() {
    private val imageString = "file:///android_asset/theme_award_beans.svga"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_replace_resource)
        glideLoad()
        loadBySvgaLib()
    }

    private fun glideLoad() {
        val dynamicEntity = SVGADynamicEntity()
        val textPaint = TextPaint()
        textPaint.color = Color.WHITE //字体颜色

        textPaint.textSize = 24f //字体大小

        textPaint.setShadowLayer(3f, 2f, 2f, -0x1000000) //字体阴影，不需要可以不用设置

        dynamicEntity.setDynamicText(
            "30",
            textPaint,
            "text_day"
        )
        Glide.with(this).load(imageString).addListener(object : RequestListener<Drawable> {
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
                if (resource is SVGAAnimationDrawable) {
                    resource.resetDynamicEntity(dynamicEntity)
                }
                return false
            }
        }).into(glideSVGAImg)
    }

    /**
     * SVGAImageView 只支持在xml中提前设置 图片路径 如果在代码中需要自自己创建解析对象
     * */
    private fun loadBySvgaLib() {
        val dynamicEntity = SVGADynamicEntity()
        val textPaint = TextPaint()
        textPaint.color = Color.BLACK //字体颜色

        textPaint.textSize = 24f //字体大小

        textPaint.setShadowLayer(3f, 2f, 2f, -0x1000000) //字体阴影，不需要可以不用设置

        dynamicEntity.setDynamicText("100",
            textPaint,
            "text_day")
        val parse = SVGAParser(this)
        parse.decodeFromAssets("theme_award_beans.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem,dynamicEntity)
                SVGAImageView.setImageDrawable(drawable)
                SVGAImageView.startAnimation()
            }

            override fun onError() {

            }
        })
    }
}