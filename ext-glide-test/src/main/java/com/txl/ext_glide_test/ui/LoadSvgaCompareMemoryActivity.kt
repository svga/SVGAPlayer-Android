package com.txl.ext_glide_test.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.txl.ext_glide_test.R
import com.txl.glide.model.SVGAModel
import kotlinx.android.synthetic.main.activity_load_asset_svga.*
import java.net.URL

/**
 * 统一个SVGA 图片内存对比
 * */
class LoadSvgaCompareMemoryActivity : AppCompatActivity() {
    private val imageString = "file:///android_asset/MerryChristmas.svga"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_svga_compare_memory)
//        loadBySvgaLib()
        Glide.with(this).load(SVGAModel(imageString, repeatCount = 0)).into(glideSVGAImg)
    }

    /**
     * SVGAImageView 只支持在xml中提前设置 图片路径 如果在代码中需要自自己创建解析对象
     * */
    private fun loadBySvgaLib() {
        val parse = SVGAParser(this)
        parse.decodeFromAssets("MerryChristmas.svga", object : SVGAParser.ParseCompletion {
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