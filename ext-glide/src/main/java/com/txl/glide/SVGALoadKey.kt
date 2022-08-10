package com.txl.glide


import com.txl.glide.model.SVGAModel
import java.io.InputStream
import java.io.Serializable

class SVGALoadKey(val svgaModel: SVGAModel, var inputStream: InputStream?):Serializable {

}