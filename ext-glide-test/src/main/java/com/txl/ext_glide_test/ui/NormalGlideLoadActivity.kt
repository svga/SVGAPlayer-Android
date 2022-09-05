package com.txl.ext_glide_test.ui

import android.animation.ValueAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.txl.ext_glide_test.R
import com.txl.glide.model.SVGAModel
import kotlinx.android.synthetic.main.activity_normal_glide_load.*

class NormalGlideLoadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_glide_load)
        //把正常图片当做svga加载
        val imgPath = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.jj20.com%2Fup%2Fallimg%2F4k%2Fs%2F02%2F2109242332225H9-0-lp.jpg&refer=http%3A%2F%2Fimg.jj20.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1664613610&t=42463a3e21c7c1a1852095299e87d031"
//        Glide.with(this).load(SVGAModel(imgPath,repeatMode = ValueAnimator.REVERSE)).into(image)
        Glide.with(this).load(imgPath).into(image)
    }
}