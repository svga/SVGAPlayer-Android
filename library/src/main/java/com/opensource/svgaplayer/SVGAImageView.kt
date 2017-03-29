package com.opensource.svgaplayer

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Choreographer
import android.view.ViewPropertyAnimator
import android.view.animation.LinearInterpolator
import android.widget.ImageView

/**
 * Created by cuiminghui on 2017/3/29.
 */

class SVGADrawable(val videoItem: SVGAVideoEntity): Drawable() {

    var cleared = true

    var currentFrame = 0
        internal set

    override fun draw(canvas: Canvas?) {
        if (cleared) {
            return
        }
        canvas?.let {
            val drawer = SVGACanvasDrawer(videoItem, it)
            drawer.drawFrame(currentFrame)
        }
    }

    override fun setAlpha(alpha: Int) { }

    override fun getOpacity(): Int {
        return 255
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

}

class SVGAImageView : ImageView {

    var loops = 0

    var clearsAfterStop = true

    private var animator: ValueAnimator? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

    fun startAnimation() {
        val drawable = drawable as? SVGADrawable ?: return
        drawable.cleared = false
        drawable.videoItem?.let {
            var durationScale = 1.0
            val animator = ValueAnimator.ofInt(0, it.frames - 1)
            Class.forName("android.animation.ValueAnimator")?.let {
                it.getDeclaredField("sDurationScale")?.let {
                    it.isAccessible = true
                    it.getFloat(Class.forName("android.animation.ValueAnimator"))?.let {
                        durationScale = it.toDouble()
                    }
                }
            }
            animator.interpolator = LinearInterpolator()
            animator.duration = (it.frames * (1000 / it.FPS) / durationScale).toLong()
            animator.repeatCount = if (loops <= 0) 99999 else loops - 1
            animator.addUpdateListener {
                drawable.currentFrame = animator.animatedValue as Int
                drawable.invalidateSelf()
            }
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    stopAnimation()
                }
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}
            })
            animator.start()
            this.animator = animator
        }
    }

    fun pauseAnimation() {
        stopAnimation(false)
    }

    fun stopAnimation() {
        stopAnimation(clear = clearsAfterStop)
    }

    fun stopAnimation(clear: Boolean) {
        animator?.cancel()
        (drawable as? SVGADrawable)?.let {
            it.cleared = clear
            it.invalidateSelf()
        }
    }

    fun setVideoItem(videoItem: SVGAVideoEntity) {
        setImageDrawable(SVGADrawable(videoItem))
    }

}