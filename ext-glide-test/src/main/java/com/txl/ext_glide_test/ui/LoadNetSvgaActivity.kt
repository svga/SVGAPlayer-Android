package com.txl.ext_glide_test.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.txl.ext_glide_test.R
import kotlinx.android.synthetic.main.activity_load_asset_svga.*
import java.net.URL

/**
 * 加载网络的SVGA图片
 * */
class LoadNetSvgaActivity : AppCompatActivity() {
    // FIXME: 没找到合适的资源
    private val imageString = "https://github.com/xiaolutang/SVGAPlayer-Android/blob/feature/txl/extglide/ext-glide-test/src/main/assets/theme_award_beans.svga"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_net_svga)
        Glide.with(this).load(imageString).into(glideSVGAImg)
        loadBySvgaLib()
    }

    /**
     * SVGAImageView 只支持在xml中提前设置 图片路径 如果在代码中需要自自己创建解析对象
     * */
    private fun loadBySvgaLib() {
        val parse = SVGAParser(this)
        parse.decodeFromURL(URL(imageString), object : SVGAParser.ParseCompletion {
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