package com.opensource.svgaplayer_react

import android.content.Context
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import java.net.URL

/**
 * Created by cuiminghui on 2017/6/16.
 */
class RCTSVGAPlayerManager: SimpleViewManager<RCTSVGAImageView>() {

    override fun getName(): String {
        return "SVGAPlayer"
    }

    override fun createViewInstance(reactContext: ThemedReactContext?): RCTSVGAImageView {
        return RCTSVGAImageView(reactContext as Context)
    }

    @ReactProp(name = "source")
    fun setSource(view: RCTSVGAImageView, source: String) {
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
        else {
            try {
                SVGAParser(view.context).parse(source, object: SVGAParser.ParseCompletion {
                    override fun onComplete(videoItem: SVGAVideoEntity) {
                        view.setVideoItem(videoItem)
                        view.startAnimation()
                    }
                    override fun onError() { }
                })
            } catch (e: Exception) {}
        }
    }

    @ReactProp(name = "loops", defaultInt = 0)
    fun setLoops(view: RCTSVGAImageView, loops: Int) {
        view.loops = loops
    }

    @ReactProp(name = "clearsAfterStop", defaultBoolean = true)
    fun setClearsAfterStop(view: RCTSVGAImageView, clearsAfterStop: Boolean) {
        view.clearsAfterStop = clearsAfterStop
    }

    @ReactProp(name = "currentState")
    fun setCurrentState(view: RCTSVGAImageView, currentState: String) {
        view.currentState = currentState
        when (currentState) {
            "start" -> {
                view.startAnimation()
            }
            "pause" -> {
                view.pauseAnimation()
            }
            "stop" -> {
                view.stopAnimation()
            }
            "clear" -> {
                view.stopAnimation(true)
            }
            else -> {

            }
        }
    }

    @ReactProp(name = "toFrame", defaultInt = -1)
    fun setToFrame(view: RCTSVGAImageView, toFrame: Int) {
        if (toFrame < 0) {
            return
        }
        view.stepToFrame(toFrame, view.currentState?.equals("play") ?: false)
    }

    @ReactProp(name = "toPercentage", defaultFloat = -1.0f)
    fun setToPercentage(view: RCTSVGAImageView, toPercentage: Float) {
        if (toPercentage < 0) {
            return
        }
        view.stepToPercentage(toPercentage.toDouble(), view.currentState?.equals("play") ?: false)
    }

}