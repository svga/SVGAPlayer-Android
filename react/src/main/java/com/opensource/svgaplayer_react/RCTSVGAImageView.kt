package com.opensource.svgaplayer_react

import android.content.Context
import android.util.AttributeSet
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGAImageView

/**
 * Created by cuiminghui on 2017/6/16.
 */
class RCTSVGAImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SVGAImageView(context, attrs, defStyleAttr) {

    internal var currentState: String? = null

    init {
        callback = object : SVGACallback {

            override fun onPause() { }

            override fun onFinished() {
                (context as? ReactContext)?.let {
                    it.getJSModule(RCTEventEmitter::class.java).receiveEvent(id, "onFinished", Arguments.createMap())
                }
            }

            override fun onRepeat() { }

            override fun onStep(frame: Int, percentage: Double) {
                (context as? ReactContext)?.let {
                    val map = Arguments.createMap()
                    map.putInt("value", frame)
                    it.getJSModule(RCTEventEmitter::class.java).receiveEvent(id, "onFrame", map)
                }
                (context as? ReactContext)?.let {
                    val map = Arguments.createMap()
                    map.putDouble("value", percentage)
                    it.getJSModule(RCTEventEmitter::class.java).receiveEvent(id, "onPercentage", map)
                }
            }

        }
    }

}