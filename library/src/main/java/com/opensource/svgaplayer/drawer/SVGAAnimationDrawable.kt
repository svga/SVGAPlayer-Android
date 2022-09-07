package com.opensource.svgaplayer.drawer

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGASoundManager
import com.opensource.svgaplayer.SVGAVideoEntity

/**
 * 当同一个SVGA图片被加载的时候 如果此时svga动画在运行中他们会共享同样的动画效果
 *
 * ***/
class SVGAAnimationDrawable(
    private val videoItem: SVGAVideoEntity,
    private val repeatCount:Int,
    private val repeatMode:Int,
    private val dynamicItem: SVGADynamicEntity): Animatable, Drawable(),
    ValueAnimator.AnimatorUpdateListener {

    companion object{
        const val TAG = "SVGAAnimationDrawable"
    }
    var animatorListener: Animator.AnimatorListener? = null
        set(value) {
            mAnimator?.removeListener(field)
            field = value
            value?.let {
                mAnimator?.addListener(it)
            }

        }

    private var mAnimator: ValueAnimator? = null
    private var currentFrame = 0
    //一共有多少帧
    private var totalFrame = 0

    private var drawer = SVGACanvasDrawer(videoItem, dynamicItem).apply {
        scaleBySelf = false
    }

    var scaleType = ImageView.ScaleType.MATRIX

    fun resetDynamicEntity(dynamicItem: SVGADynamicEntity){
        drawer = SVGACanvasDrawer(videoItem, dynamicItem).apply {
            scaleBySelf = false
        }
    }

    override fun getIntrinsicWidth(): Int {
        return videoItem.videoSize.width.toInt()
    }

    override fun getIntrinsicHeight(): Int {
        return videoItem.videoSize.height.toInt()
    }

    override fun start() {
        Log.d(TAG,"start")
        if(mAnimator == null || mAnimator?.isRunning == false){
            val startFrame = 0
            val endFrame = videoItem.frames - 1
            totalFrame = (endFrame - startFrame + 1)
            mAnimator = ValueAnimator.ofInt(startFrame,endFrame)
            mAnimator?.interpolator = LinearInterpolator()
            mAnimator?.duration = (totalFrame * (1000 / videoItem.FPS) / generateScale()).toLong()
            mAnimator?.repeatCount = repeatCount
            mAnimator?.repeatMode = repeatMode
            mAnimator?.addUpdateListener(this)
            animatorListener?.let {
                mAnimator?.addListener(it)
            }

            mAnimator?.start()
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mAnimator?.resume()
            }else{
                mAnimator?.start()
            }
        }
    }

    private fun generateScale(): Double {
        var scale = 1.0
        try {
            val animatorClass = Class.forName("android.animation.ValueAnimator") ?: return scale
            val getMethod = animatorClass.getDeclaredMethod("getDurationScale") ?: return scale
            scale = (getMethod.invoke(animatorClass) as Float).toDouble()
            if (scale == 0.0) {
                val setMethod = animatorClass.getDeclaredMethod("setDurationScale",Float::class.java) ?: return scale
                setMethod.isAccessible = true
                setMethod.invoke(animatorClass,1.0f)
                scale = 1.0
            }
        } catch (ignore: Exception) {
            ignore.printStackTrace()
        }
        return scale
    }


    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        return super.setVisible(visible, restart)
    }

    override fun stop() {
        Log.d(TAG,"stop")
        mAnimator?.cancel()
        mAnimator = null

        videoItem.audioList.forEach { audio ->
            audio.playID?.let {
                if (SVGASoundManager.isInit()){
                    SVGASoundManager.pause(it)
                }else{
                    videoItem.soundPool?.pause(it)
                }
            }
        }
    }

    override fun isRunning(): Boolean {
        return mAnimator?.isRunning == true
    }

    override fun draw(canvas: Canvas) {
        drawer.drawFrame(canvas,currentFrame,scaleType)
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        currentFrame = animation?.animatedValue as Int
//        Log.d(TAG,"onAnimationUpdate currentFrame :  $currentFrame")
        invalidateSelf()
    }

    // FIXME: 完成资源回收
    fun recycle(){
        clear()
    }

    private fun clear() {
        videoItem.audioList.forEach { audio ->
            audio.playID?.let {
                if (SVGASoundManager.isInit()){
                    SVGASoundManager.stop(it)
                }else{
                    videoItem.soundPool?.stop(it)
                }
            }
            audio.playID = null
        }
        videoItem.clear()
    }
}