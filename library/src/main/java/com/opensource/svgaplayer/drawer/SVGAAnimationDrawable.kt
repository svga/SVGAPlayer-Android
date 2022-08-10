package com.opensource.svgaplayer.drawer

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
import com.opensource.svgaplayer.SVGAVideoEntity

class SVGAAnimationDrawable(private val videoItem: SVGAVideoEntity, private val dynamicItem: SVGADynamicEntity = SVGADynamicEntity()): Animatable, Drawable(),
    ValueAnimator.AnimatorUpdateListener {

    companion object{
        const val TAG = "SVGAAnimationDrawable"
    }

    var loops = 0
    private var mAnimator: ValueAnimator? = null
    private var currentFrame = 0

    private val drawer = SVGACanvasDrawer(videoItem, dynamicItem).apply {
        scaleBySelf = false
    }

    var scaleType = ImageView.ScaleType.MATRIX

    override fun getIntrinsicWidth(): Int {
        return videoItem.videoSize.width.toInt()
    }

    override fun getIntrinsicHeight(): Int {
        return videoItem.videoSize.height.toInt()
    }

    override fun start() {
        Log.d(TAG,"start")
        if(mAnimator == null){
            val startFrame = 0
            val endFrame = videoItem.frames - 1
            mAnimator = ValueAnimator.ofInt(startFrame,endFrame)
            mAnimator?.interpolator = LinearInterpolator()
            mAnimator?.duration = ((endFrame - startFrame + 1) * (1000 / videoItem.FPS) / generateScale()).toLong()
            mAnimator?.repeatCount = if (loops <= 0) 99999 else loops - 1
            mAnimator?.addUpdateListener(this)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAnimator?.pause()
        }else{
            mAnimator?.cancel()
            mAnimator = null
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
        invalidateSelf()
    }

    // FIXME: 完成资源回收
    fun recycle(){

    }
}