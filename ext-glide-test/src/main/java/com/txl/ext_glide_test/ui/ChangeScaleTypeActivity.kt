package com.txl.ext_glide_test.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.txl.ext_glide_test.R
import kotlinx.android.synthetic.main.activity_change_scale_type.*
import kotlinx.android.synthetic.main.activity_load_asset_svga.*
import kotlinx.android.synthetic.main.activity_load_asset_svga.SVGAImageView
import kotlinx.android.synthetic.main.activity_load_asset_svga.glideSVGAImg

class ChangeScaleTypeActivity : AppCompatActivity() {
    private val imageString = "file:///android_asset/theme_award_beans.svga"
    private var scaleTypeIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_scale_type)
        Glide.with(this).load(imageString).into(glideSVGAImg)
        loadBySvgaLib()
        tvChangeScaleType.setOnClickListener {
            scaleTypeIndex++
            val scaleType = getScaleType(scaleTypeIndex)
            glideSVGAImg.scaleType = scaleType
            SVGAImageView.scaleType = scaleType
            tvCurrentScaleType.text = "当前缩放模式 $scaleType"
        }
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

    private fun getScaleType(index: Int): ImageView.ScaleType {
        when (index % 7) {
            0 -> {
                return ImageView.ScaleType.MATRIX
            }
            1 -> {
                return ImageView.ScaleType.FIT_XY
            }
            2 -> {
                return ImageView.ScaleType.FIT_START
            }
            3 -> {
                return ImageView.ScaleType.FIT_CENTER
            }
            4 -> {
                return ImageView.ScaleType.FIT_END
            }
            5 -> {
                return ImageView.ScaleType.CENTER
            }
            6 -> {
                return ImageView.ScaleType.CENTER_CROP
            }
            7 -> {
                return ImageView.ScaleType.CENTER_INSIDE
            }
        }
        return ImageView.ScaleType.CENTER_CROP
    }
}