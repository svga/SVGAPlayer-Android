package com.txl.glide.model

import java.io.Serializable


/**
 * 定义SVGA 加载模型 比如动画重复次数
 * 动画监听对象
 * 文本替换对象
 * @param path 加载的实际数据类型 比如 String，Uri
 * @param typeClass 指定加载资源的类型  需要与type 的class保持一致
 * */
open class SVGAModel(val path:Any,val typeClass: SVGALoadType):ISVGAModel,Serializable {

}

enum class SVGALoadType{
    String,
    Uri
}