package com.txl.ext_glide_test

import android.app.Application
import com.txl.glide.model.SVGALoader

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SVGALoader.init(this)
    }
}