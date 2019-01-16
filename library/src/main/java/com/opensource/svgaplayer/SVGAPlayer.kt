package com.opensource.svgaplayer

import android.content.Context
import android.util.AttributeSet

/**
 * Created by cuiminghui on 2017/3/30.
 * @deprecated from 2.4.0
 */
@Deprecated("This class has been deprecated from 2.4.0. We don't recommend you to use it.")
class SVGAPlayer: SVGAImageView {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

}