package com.opensource.svgaplayer

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.io.Serializable
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by PonyCui_Home on 16/6/18.
 */
class SVGAVideoEntity {

    var antiAlias = false

    var videoSize = SVGARect(0.0, 0.0, 0.0, 0.0)
        private set

    var FPS = 15
        private set

    var frames: Int = 0
        private set

    var sprites: List<SVGAVideoSpriteEntity> = listOf()
        private set

    var images = HashMap<String, Bitmap>()
        private set

    private var cacheDir: File

    constructor(obj: JSONObject, cacheDir: File) {
        this.cacheDir = cacheDir
        obj.optJSONObject("movie")?.let {
            it.optJSONObject("viewBox")?.let {
                videoSize = SVGARect(0.0, 0.0, it.optDouble("width", 0.0), it.optDouble("height", 0.0))
            }
            FPS = it.optInt("fps", 20)
            frames = it.optInt("frames", 0)
        }
        resetImages(obj)
        resetSprites(obj)
    }

    constructor(obj: ComOpensourceSvgaVideo.MovieEntity, cacheDir: File) {
        this.cacheDir = cacheDir
        if (obj.hasParams()) {
            obj.params?.let { movieParams ->
                videoSize = SVGARect(0.0, 0.0, movieParams.viewBoxWidth.toDouble(), movieParams.viewBoxHeight.toDouble())
                FPS = movieParams.fps
                frames = movieParams.frames
            }
        }
        resetImages(obj)
        resetSprites(obj)
    }

    private fun resetImages(obj: JSONObject) {
        obj.optJSONObject("images")?.let {
            val imgObjects = it
            it.keys().forEach {
                val imageKey = it
                var filePath = cacheDir.absolutePath + "/" + imgObjects[imageKey]
                var bitmap = if (File(filePath).exists()) BitmapFactory.decodeFile(filePath) else null
                if (bitmap != null) {
                    images.put(imageKey, bitmap)
                }
                else {
                    filePath = cacheDir.absolutePath + "/" + imageKey + ".png"
                    bitmap = if (File(filePath).exists()) BitmapFactory.decodeFile(filePath) else null
                    if (bitmap != null) {
                        images.put(imageKey, bitmap)
                    }
                }
            }
        }
    }

    private fun resetImages(obj: ComOpensourceSvgaVideo.MovieEntity) {
        obj.imagesMap.entries.forEach {
            val imageKey = it.key
            var filePath = cacheDir.absolutePath + "/" + it.value
            var bitmap = if (File(filePath).exists()) BitmapFactory.decodeFile(filePath) else null
            if (bitmap != null) {
                images.put(imageKey, bitmap)
            }
            else {
                filePath = cacheDir.absolutePath + "/" + imageKey + ".png"
                bitmap = if (File(filePath).exists()) BitmapFactory.decodeFile(filePath) else null
                if (bitmap != null) {
                    images.put(imageKey, bitmap)
                }
            }
        }
    }

    private fun resetSprites(obj: JSONObject) {
        val mutableList: MutableList<SVGAVideoSpriteEntity> = mutableListOf()
        obj.optJSONArray("sprites")?.let {
            for (i in 0 until it.length()) {
                it.optJSONObject(i)?.let {
                    mutableList.add(SVGAVideoSpriteEntity(it))
                }
            }
        }
        sprites = mutableList.toList()
    }

    private fun resetSprites(obj: ComOpensourceSvgaVideo.MovieEntity) {
        sprites = obj.spritesList.map {
            return@map SVGAVideoSpriteEntity(it)
        }
    }

}

