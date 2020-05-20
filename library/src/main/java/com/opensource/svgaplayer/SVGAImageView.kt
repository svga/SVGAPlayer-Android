package com.opensource.svgaplayer

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.opensource.svgaplayer.utils.SVGARange
import java.lang.ref.WeakReference
import java.net.URL

/**
 * Created by PonyCui on 2017/3/29.
 */
open class SVGAImageView : ImageView {

    enum class FillMode {
        Backward,
        Forward,
    }

    var isAnimating = false
        private set

    var loops = 0
    var clearsAfterStop = true
    var fillMode: FillMode = FillMode.Forward
    var callback: SVGACallback? = null

    private var mVideoItem: SVGAVideoEntity? = null
    private var mAnimator: ValueAnimator? = null
    private var mItemClickAreaListener : SVGAClickAreaListener? = null
    private var mAntiAlias = true
    private var mAutoPlay = true
    private var mDrawable: SVGADrawable? = null
    private val mAnimatorListener = AnimatorListener(this)
    private val mAnimatorUpdateListener = AnimatorUpdateListener(this)
    private var mStartFrame = 0
    private var mEndFrame = 0

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

    private fun loadAttrs(attrs: AttributeSet) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SVGAImageView, 0, 0)
        loops = typedArray.getInt(R.styleable.SVGAImageView_loopCount, 0)
        clearsAfterStop = typedArray.getBoolean(R.styleable.SVGAImageView_clearsAfterStop, true)
        mAntiAlias = typedArray.getBoolean(R.styleable.SVGAImageView_antiAlias, true)
        mAutoPlay = typedArray.getBoolean(R.styleable.SVGAImageView_autoPlay, true)
        typedArray.getString(R.styleable.SVGAImageView_fillMode)?.let {
            if (it == "0") {
                fillMode = FillMode.Backward
            }
            else if (it == "1") {
                fillMode = FillMode.Forward
            }
        }
        typedArray.getString(R.styleable.SVGAImageView_source)?.let {
            ParserSourceThread(this, it).start()
        }
        typedArray.recycle()
    }

    private fun startAnimation(videoItem: SVGAVideoEntity) {
        this@SVGAImageView.post {
            videoItem.antiAlias = mAntiAlias
            setVideoItem(videoItem)
            (drawable as? SVGADrawable)?.scaleType = scaleType
            if (mAutoPlay) {
                startAnimation()
            }
        }
    }

    fun startAnimation() {
        startAnimation(null, false)
    }

    fun startAnimation(range: SVGARange?, reverse: Boolean = false) {
        stopAnimation(false)
        val drawable = drawable as? SVGADrawable ?: return
        drawable.cleared = false
        drawable.scaleType = scaleType
        play(range, drawable.videoItem, drawable, reverse)
    }

    private fun play(range: SVGARange?, it: SVGAVideoEntity, drawable: SVGADrawable, reverse: Boolean) {
        mDrawable = drawable;
        mStartFrame = Math.max(0, range?.location ?: 0)
        mEndFrame = Math.min(it.frames - 1, ((range?.location ?: 0) + (range?.length
                ?: Int.MAX_VALUE) - 1))
        val animator = ValueAnimator.ofInt(mStartFrame, mEndFrame)
        animator.interpolator = LinearInterpolator()
        animator.duration = ((mEndFrame - mStartFrame + 1) * (1000 / it.FPS) / generateScale()).toLong()
        animator.repeatCount = if (loops <= 0) 99999 else loops - 1
        animator.addUpdateListener(mAnimatorUpdateListener)
        animator.addListener(mAnimatorListener);
        if (reverse) {
            animator.reverse()
        } else {
            animator.start()
        }
        mAnimator = animator
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
    private fun generateScale(): Double {
        var scale = 1.0
        try {
            val animatorClass = Class.forName("android.animation.ValueAnimator")
            animatorClass?.let { clazz ->
                clazz.getDeclaredField("sDurationScale")?.let { field ->
                    field.isAccessible = true
                    field.getFloat(animatorClass).let { value ->
                        scale = value.toDouble()
                    }
                    if (scale == 0.0) {
                        field.setFloat(animatorClass, 1.0f)
                        scale = 1.0
                        Log.e("SVGAPlayer", "The animation duration scale has been reset to 1.0x," +
                                " because you closed it on developer options.")
                    }
                }
            }
        } catch (ignore: Exception) {
        }
        return scale
    }

    private fun onAnimatorUpdate(animator: ValueAnimator?) {
        if (mDrawable == null) {
            return
        }
        mDrawable!!.currentFrame = animator?.animatedValue as Int
        val percentage = (mDrawable!!.currentFrame + 1).toDouble() / mDrawable!!.videoItem.frames.toDouble()
        callback?.onStep(mDrawable!!.currentFrame, percentage)
    }

    private fun onAnimationEnd(animation: Animator?) {
        isAnimating = false
        stopAnimation()
        if (!clearsAfterStop) {
            if (fillMode == FillMode.Backward) {
                mDrawable!!.currentFrame = mStartFrame
            } else if (fillMode == FillMode.Forward) {
                mDrawable!!.currentFrame = mEndFrame
            }
        }
        callback?.onFinished()
    }

    fun pauseAnimation() {
        stopAnimation(false)
        callback?.onPause()
    }

    fun stopAnimation() {
        stopAnimation(clear = clearsAfterStop)
    }

    fun stopAnimation(clear: Boolean) {
        mAnimator?.cancel()
        mAnimator?.removeAllListeners()
        mAnimator?.removeAllUpdateListeners()
        (drawable as? SVGADrawable)?.let {
            it.cleared = clear
        }
    }

    fun setVideoItem(videoItem: SVGAVideoEntity?) {
        setVideoItem(videoItem, SVGADynamicEntity())
    }

    fun setVideoItem(videoItem: SVGAVideoEntity?, dynamicItem: SVGADynamicEntity?) {
        if (videoItem == null) {
            setImageDrawable(null)
            return
        }
        val drawable = SVGADrawable(videoItem, dynamicItem ?: SVGADynamicEntity())
        drawable.cleared = clearsAfterStop
        setImageDrawable(drawable)
        this.mVideoItem = videoItem
    }

    fun stepToFrame(frame: Int, andPlay: Boolean) {
        pauseAnimation()
        val drawable = drawable as? SVGADrawable ?: return
        drawable.currentFrame = frame
        if (andPlay) {
            startAnimation()
            mAnimator?.let {
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

    fun setOnAnimKeyClickListener(clickListener : SVGAClickAreaListener){
        mItemClickAreaListener = clickListener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mVideoItem == null) {
            return super.onTouchEvent(event)
        }
        event?.let {
            if(event.action == MotionEvent.ACTION_DOWN){
                val drawable = drawable as? SVGADrawable ?: return false
                for((key,value) in drawable.dynamicItem.mClickMap){
                    if (event.x >= value[0] && event.x <= value[2] && event.y >= value[1] && event.y <= value[3]) {
                        mItemClickAreaListener?.let {
                            it.onClick(key)
                            return true
                        }
                    }
                }
            }
        }

        return super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearAudio()
        mAnimator?.cancel()
        mAnimator?.removeAllListeners()
        mAnimator?.removeAllUpdateListeners()
    }

    private fun clearAudio() {
        this.mVideoItem?.audios?.forEach { audio ->
            audio.playID?.let {
                this.mVideoItem?.soundPool?.stop(it)
            }
            audio.playID = null
        }
    }

    /**
     * 解析资源线程，不持有外部引用
     */
    private class ParserSourceThread(view: SVGAImageView, val source: String) : Thread() {
        /**
         * 使用弱引用解决内存泄漏
         */
        private val weakReference = WeakReference<SVGAImageView>(view)
        private val parser = SVGAParser(view.context)

        override fun run() {
            if (source.startsWith("http://") || source.startsWith("https://")) {
                parser.parse(URL(source), createCallback())
            } else {
                parser.parse(source, createCallback())
            }
        }

        private fun createCallback(): SVGAParser.ParseCompletion {
            return object : SVGAParser.ParseCompletion {
                override fun onComplete(videoItem: SVGAVideoEntity) {
                    weakReference.get()?.startAnimation(videoItem)
                }

                override fun onError() {}
            }
        }
    } // end of ParserSourceThread

    private class AnimatorListener(view: SVGAImageView) : Animator.AnimatorListener {
        private val weakReference = WeakReference<SVGAImageView>(view)

        override fun onAnimationRepeat(animation: Animator?) {
            weakReference.get()?.callback?.onRepeat()
        }

        override fun onAnimationEnd(animation: Animator?) {
            weakReference.get()?.onAnimationEnd(animation)
        }

        override fun onAnimationCancel(animation: Animator?) {
            weakReference.get()?.isAnimating = false
        }

        override fun onAnimationStart(animation: Animator?) {
            weakReference.get()?.isAnimating = true
        }
    } // end of AnimatorListener


    private class AnimatorUpdateListener(view: SVGAImageView) : ValueAnimator.AnimatorUpdateListener {
        private val weakReference = WeakReference<SVGAImageView>(view)

        override fun onAnimationUpdate(animation: ValueAnimator?) {
            weakReference.get()?.onAnimatorUpdate(animation)
        }
    } // end of AnimatorUpdateListener
}
