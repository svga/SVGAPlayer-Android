package com.opensource.svgaplayer

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import java.net.URL
import java.util.*

/**
 * Created by cuiminghui on 2017/3/29.
 */

class SVGADrawable(val videoItem: SVGAVideoEntity, val dynamicItem: SVGADynamicEntity): Drawable() {

    constructor(videoItem: SVGAVideoEntity): this(videoItem, SVGADynamicEntity())

    var cleared = true
        internal set (value) {
            if (field == value) {
                return
            }
            field = value
            invalidateSelf()
        }

    var currentFrame = 0
        internal set (value) {
            if (field == value) {
                return
            }
            field = value
            invalidateSelf()
        }

    var scaleType: ImageView.ScaleType = ImageView.ScaleType.MATRIX

    internal val drawer = SVGACanvasDrawer(videoItem, dynamicItem)

    override fun draw(canvas: Canvas?) {
        if (cleared) {
            return
        }
        canvas?.let {
            drawer.canvas = it
            drawer.drawFrame(currentFrame, scaleType)
        }
    }

    override fun setAlpha(alpha: Int) { }

    override fun getOpacity(): Int {
        return 255
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

}

open class SVGAImageView : ImageView {

    enum class FillMode {
        Backward,
        Forward,
    }

    var loops = 0

    var clearsAfterStop = true

    var fillMode: FillMode = FillMode.Forward

    var callback: SVGACallback? = null

    private var animator: ValueAnimator? = null

    constructor(context: Context?) : super(context) {
        setSoftwareLayerType()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setSoftwareLayerType()
        attrs?.let { loadAttrs(it) }
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setSoftwareLayerType()
        attrs?.let { loadAttrs(it) }
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        setSoftwareLayerType()
        attrs?.let { loadAttrs(it) }
    }

    private fun setSoftwareLayerType() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        animator?.removeAllUpdateListeners()
    }

    fun loadAttrs(attrs: AttributeSet) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SVGAImageView, 0, 0)
        loops = typedArray.getInt(R.styleable.SVGAImageView_loopCount, 0)
        clearsAfterStop = typedArray.getBoolean(R.styleable.SVGAImageView_clearsAfterStop, true)
        val antiAlias = typedArray.getBoolean(R.styleable.SVGAImageView_antiAlias, false)
        typedArray.getString(R.styleable.SVGAImageView_source)?.let {
            val parser = SVGAParser(context)
            Thread({
                if(it.startsWith("http://") || it.startsWith("https://")) {
                    URL(it)?.let {
                        parser.parse(it, object : SVGAParser.ParseCompletion {
                            override fun onComplete(videoItem: SVGAVideoEntity) {
                                handler?.post {
                                    videoItem.antiAlias = antiAlias
                                    setVideoItem(videoItem)
                                    if (typedArray.getBoolean(R.styleable.SVGAImageView_autoPlay, true)) {
                                        startAnimation()
                                    }
                                }
                            }
                            override fun onError() { }
                        })
                        return@Thread
                    }
                }
                parser.parse(it, object : SVGAParser.ParseCompletion {
                    override fun onComplete(videoItem: SVGAVideoEntity) {
                        handler?.post {
                            videoItem.antiAlias = antiAlias
                            setVideoItem(videoItem)
                            if (typedArray.getBoolean(R.styleable.SVGAImageView_autoPlay, true)) {
                                startAnimation()
                            }
                        }
                    }
                    override fun onError() { }
                })
            }).start()
        }
        typedArray.getString(R.styleable.SVGAImageView_fillMode)?.let {
            if (it.equals("0")) {
                fillMode = FillMode.Backward
            }
            else if (it.equals("1")) {
                fillMode = FillMode.Forward
            }
        }
    }

    fun startAnimation() {
        val drawable = drawable as? SVGADrawable ?: return
        drawable.cleared = false
        drawable.scaleType = scaleType
        drawable.videoItem?.let {
            var durationScale = 1.0
            val animator = ValueAnimator.ofInt(0, it.frames - 1)
            try {
                Class.forName("android.animation.ValueAnimator")?.let {
                    it.getDeclaredField("sDurationScale")?.let {
                        it.isAccessible = true
                        it.getFloat(Class.forName("android.animation.ValueAnimator"))?.let {
                            durationScale = it.toDouble()
                        }
                    }
                }
            } catch (e: Exception) {}
            animator.interpolator = LinearInterpolator()
            animator.duration = (it.frames * (1000 / it.FPS) / durationScale).toLong()
            animator.repeatCount = if (loops <= 0) 99999 else loops - 1
            animator.addUpdateListener {
                drawable.currentFrame = animator.animatedValue as Int
                callback?.onStep(drawable.currentFrame, ((drawable.currentFrame + 1).toDouble() / drawable.videoItem.frames.toDouble()))
            }
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                    callback?.onRepeat()
                }
                override fun onAnimationEnd(animation: Animator?) {
                    stopAnimation()
                    if (!clearsAfterStop) {
                        if (fillMode == FillMode.Backward) {
                            drawable.currentFrame = 0
                        }
                    }
                    callback?.onFinished()
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
        callback?.onPause()
    }

    fun stopAnimation() {
        stopAnimation(clear = clearsAfterStop)
    }

    fun stopAnimation(clear: Boolean) {
        animator?.cancel()
        animator?.removeAllUpdateListeners()
        (drawable as? SVGADrawable)?.let {
            it.cleared = clear
        }
    }

    fun setVideoItem(videoItem: SVGAVideoEntity) {
        setVideoItem(videoItem, SVGADynamicEntity())
    }

    fun setVideoItem(videoItem: SVGAVideoEntity, dynamicItem: SVGADynamicEntity) {
        val drawable = SVGADrawable(videoItem, dynamicItem)
        drawable.cleared = clearsAfterStop
        setImageDrawable(drawable)
    }

    fun stepToFrame(frame: Int, andPlay: Boolean) {
        pauseAnimation()
        val drawable = drawable as? SVGADrawable ?: return
        drawable.currentFrame = frame
        if (andPlay) {
            startAnimation()
            animator?.let {
                it.currentPlayTime = (Math.max(0.0f, Math.min(1.0f, (frame.toFloat() / drawable.videoItem.frames.toFloat()))) * it.duration).toLong()
            }
        }
    }

    fun stepToPercentage(percentage: Double, andPlay: Boolean) {
        val drawable = drawable as? SVGADrawable ?: return
        var frame = (drawable.videoItem.frames * percentage).toInt()
        if (frame >= drawable.videoItem.frames && frame > 0) {
            frame = drawable.videoItem.frames - 1
        }
        stepToFrame(frame, andPlay)
    }

}