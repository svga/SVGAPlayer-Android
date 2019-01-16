package com.opensource.svgaplayer.utils

import android.widget.ImageView

/**
 * Created by ubt on 2018/1/19.
 */
class SVGAScaleInfo {

    var tranFx : Float = 0.0f
    var tranFy : Float = 0.0f
    var scaleFx : Float = 1.0f
    var scaleFy : Float = 1.0f
    var ratio = 1.0f
    var ratioX = false

    private fun resetVar(){
        tranFx = 0.0f
        tranFy = 0.0f
        scaleFx = 1.0f
        scaleFy = 1.0f
        ratio = 1.0f
        ratioX = false
    }

    fun performScaleType(canvasWidth : Float, canvasHeight: Float, videoWidth : Float, videoHeight : Float, scaleType: ImageView.ScaleType) {
        if (canvasWidth == 0.0f || canvasHeight == 0.0f || videoWidth == 0.0f || videoHeight == 0.0f) {
            return
        }

        resetVar()
        val canW_vidW_f = (canvasWidth - videoWidth) / 2.0f
        val canH_vidH_f = (canvasHeight - videoHeight) / 2.0f

        val videoRatio = videoWidth / videoHeight
        val canvasRatio = canvasWidth / canvasHeight

        val canH_d_vidH = canvasHeight / videoHeight
        val canW_d_vidW = canvasWidth / videoWidth
        
        when (scaleType) {
            ImageView.ScaleType.CENTER -> {
                tranFx = canW_vidW_f
                tranFy = canH_vidH_f
            }
            ImageView.ScaleType.CENTER_CROP -> {
                if (videoRatio > canvasRatio) {
                    ratio = canH_d_vidH
                    ratioX = false
                    scaleFx = canH_d_vidH
                    scaleFy = canH_d_vidH
                    tranFx = (canvasWidth - videoWidth * (canH_d_vidH)) / 2.0f
                }
                else {
                    ratio = canW_d_vidW
                    ratioX = true
                    scaleFx = canW_d_vidW
                    scaleFy = canW_d_vidW
                    tranFy = (canvasHeight - videoHeight * (canW_d_vidW)) / 2.0f
                }
            }
            ImageView.ScaleType.CENTER_INSIDE -> {
                if (videoWidth < canvasWidth && videoHeight < canvasHeight) {
                    tranFx = canW_vidW_f
                    tranFy = canH_vidH_f
                }
                else {
                    if (videoRatio > canvasRatio) {
                        ratio = canW_d_vidW
                        ratioX = true
                        scaleFx = canW_d_vidW
                        scaleFy = canW_d_vidW
                        tranFy = (canvasHeight - videoHeight * (canW_d_vidW)) / 2.0f
                        
                    }
                    else {
                        ratio = canH_d_vidH
                        ratioX = false
                        scaleFx = canH_d_vidH
                        scaleFy = canH_d_vidH
                        tranFx = (canvasWidth - videoWidth * (canH_d_vidH)) / 2.0f
                    }
                }
            }
            ImageView.ScaleType.FIT_CENTER -> {
                if (videoRatio > canvasRatio) {
                    ratio = canW_d_vidW
                    ratioX = true
                    scaleFx = canW_d_vidW
                    scaleFy = canW_d_vidW
                    tranFy = (canvasHeight - videoHeight * (canW_d_vidW)) / 2.0f
                }
                else {
                    ratio = canH_d_vidH
                    ratioX = false
                    scaleFx = canH_d_vidH
                    scaleFy = canH_d_vidH
                    tranFx = (canvasWidth - videoWidth * (canH_d_vidH)) / 2.0f
                }
            }
            ImageView.ScaleType.FIT_START -> {
                if (videoRatio > canvasRatio) {
                    ratio = canW_d_vidW
                    ratioX = true
                    scaleFx = canW_d_vidW
                    scaleFy = canW_d_vidW
                }
                else {
                    ratio = canH_d_vidH
                    ratioX = false
                    scaleFx = canH_d_vidH
                    scaleFy = canH_d_vidH
                }
            }
            ImageView.ScaleType.FIT_END -> {
                if (videoRatio > canvasRatio) {
                    ratio = canW_d_vidW
                    ratioX = true
                    scaleFx = canW_d_vidW
                    scaleFy = canW_d_vidW
                    tranFy= canvasHeight - videoHeight * (canW_d_vidW)
                }
                else {
                    ratio = canH_d_vidH
                    ratioX = false
                    scaleFx = canH_d_vidH
                    scaleFy = canH_d_vidH
                    tranFx = canvasWidth - videoWidth * (canH_d_vidH)
                }
            }
            ImageView.ScaleType.FIT_XY -> {
                ratio = Math.max(canW_d_vidW, canH_d_vidH)
                ratioX = canW_d_vidW > canH_d_vidH
                scaleFx = canW_d_vidW
                scaleFy = canH_d_vidH
            }
            else -> {
                ratio = canW_d_vidW
                ratioX = true
                scaleFx = canW_d_vidW
                scaleFy = canW_d_vidW
            }
        }
    }

}
