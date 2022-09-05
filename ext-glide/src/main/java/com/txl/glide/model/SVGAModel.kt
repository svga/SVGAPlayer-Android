package com.txl.glide.model

import android.animation.ValueAnimator
import android.net.Uri
import com.opensource.svgaplayer.SVGADynamicEntity
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.RuntimeException


/**
 * 定义SVGA 加载模型 比如动画重复次数
 * 动画监听对象
 * 文本替换对象
 * @param path 加载的实际数据类型 比如 String，Uri
 * @param typeClass 指定加载资源的类型  需要与type 的class保持一致
 * @param repeatCount 重复次数
 * @param repeatMode 重复模式
 * @param markCacheKey 标记Glide缓存的key  如果想要某个资源不被重用、共享 那么给markCacheKey传递不同的值
 * */
open class SVGAModel(
    val path: Any = "",
    val typeClass: SVGALoadType = SVGALoadType.String,
    val repeatCount: Int = ValueAnimator.INFINITE,
    val repeatMode: Int = ValueAnimator.RESTART,
    val markCacheKey:String = ""
) : ISVGAModel, Serializable {

    init {
        checkSafe()
    }

    private fun checkSafe(){
        val result = when(typeClass){
            SVGALoadType.String->{
                path is String
            }
            SVGALoadType.Uri->{
                path is Uri
            }
        }
        if(!result){
            throw IllegalArgumentException("path typeClass not match please check")
        }
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + typeClass.hashCode()
        result = 31 * result + repeatCount
        result = 31 * result + repeatMode
        result = 31 * result + markCacheKey.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SVGAModel) return false

        if (path != other.path) return false
        if (typeClass != other.typeClass) return false
        if (repeatCount != other.repeatCount) return false
        if (repeatMode != other.repeatMode) return false
        if (markCacheKey != other.markCacheKey) return false

        return true
    }
}

enum class SVGALoadType {
    String,
    Uri
}