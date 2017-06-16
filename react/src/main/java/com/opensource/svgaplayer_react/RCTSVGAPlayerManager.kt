package com.opensource.svgaplayer_react

import android.content.Context
import android.support.annotation.NonNull
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import java.net.URL

/**
 * Created by cuiminghui on 2017/6/16.
 */
class RCTSVGAPlayerManager: SimpleViewManager<SVGAImageView>() {

    override fun getName(): String {
        return "SVGAPlayer"
    }

    override fun createViewInstance(reactContext: ThemedReactContext?): SVGAImageView {
        return SVGAImageView(reactContext as Context)
    }

    @ReactProp(name = "source")
    fun setSource(view: SVGAImageView, source: String) {
        if (source.startsWith("http") || source.startsWith("https")) {
            try {
                SVGAParser(view.context).parse(URL(source), object: SVGAParser.ParseCompletion {
                    override fun onComplete(videoItem: SVGAVideoEntity) {
                        view.setVideoItem(videoItem)
                        view.startAnimation()
                    }
                    override fun onError() { }
                })
            } catch (e: Exception) {}
        }
    }

}