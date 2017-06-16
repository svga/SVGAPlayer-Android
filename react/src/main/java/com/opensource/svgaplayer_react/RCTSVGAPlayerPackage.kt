package com.opensource.svgaplayer_react

import android.view.View
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ReactShadowNode
import com.facebook.react.uimanager.ViewManager

/**
 * Created by cuiminghui on 2017/6/16.
 */
class RCTSVGAPlayerPackage: ReactPackage {

    override fun createJSModules(): MutableList<Class<out JavaScriptModule>> {
        return mutableListOf()
    }

    override fun createNativeModules(reactContext: ReactApplicationContext?): MutableList<NativeModule> {
        return mutableListOf()
    }

    override fun createViewManagers(reactContext: ReactApplicationContext?): MutableList<ViewManager<View, ReactShadowNode>> {
        return mutableListOf(RCTSVGAPlayerManager() as ViewManager<View, ReactShadowNode>)
    }

}