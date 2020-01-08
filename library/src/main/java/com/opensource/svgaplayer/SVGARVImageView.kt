package com.opensource.svgaplayer

import android.content.Context
import android.util.AttributeSet

/**
 * Created by miaojun on 2020-01-08.
 * mail:1290846731@qq.com
 */
class SVGARVImageView : SVGAImageView {
    var isNeedResume = true

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if(isNeedResume && drawable != null){
            startAnimation()
        }
    }

}