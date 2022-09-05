package com.txl.ext_glide_test.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.TextPaint
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.drawer.SVGAAnimationDrawable
import com.txl.ext_glide_test.R
import com.txl.glide.model.SVGALoadType
import com.txl.glide.model.SVGAModel


class SvgaDemoActivity : AppCompatActivity() {
    private val  tag = SvgaDemoActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_svga_demo)
        initView()
    }

    private fun initView() {
        setClickListener()
    }

    private fun setClickListener() {
        findViewById<View>(R.id.tvAssetDemo).setOnClickListener {
            startActivity(Intent(this,LoadAssetSvgaActivity::class.java))
        }
        findViewById<View>(R.id.tvNetDemo).setOnClickListener {
            startActivity(Intent(this,LoadNetSvgaActivity::class.java))
        }
        findViewById<View>(R.id.tvChangeScaleTypeDemo).setOnClickListener {
            startActivity(Intent(this,ChangeScaleTypeActivity::class.java))
        }
        findViewById<View>(R.id.tvReplaceResourceDemo).setOnClickListener {
            startActivity(Intent(this,ReplaceResourceActivity::class.java))
        }
        findViewById<View>(R.id.tvSetRepeatCountDemo).setOnClickListener {
            startActivity(Intent(this,ChangeRepeatCountActivity::class.java))
        }
        findViewById<View>(R.id.tvSetRepeatModeDemo).setOnClickListener {
            startActivity(Intent(this,ChangeRepeatModeActivity::class.java))
        }
        findViewById<View>(R.id.tvAddListenerDemo).setOnClickListener {
            startActivity(Intent(this,AddListenerActivity::class.java))
        }
        findViewById<View>(R.id.tvNormalGlide).setOnClickListener {
            startActivity(Intent(this,NormalGlideLoadActivity::class.java))
        }
        findViewById<View>(R.id.tvPlayAudio).setOnClickListener {
            startActivity(Intent(this,LoadAudioSvgaActivity::class.java))
        }
        findViewById<View>(R.id.tvCompareMemory).setOnClickListener {
            startActivity(Intent(this,LoadSvgaCompareMemoryActivity::class.java))
        }
    }
}