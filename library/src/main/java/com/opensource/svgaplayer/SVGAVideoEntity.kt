package com.opensource.svgaplayer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.opensource.svgaplayer.proto.MovieEntity
import org.json.JSONObject
import java.io.File
import java.util.*

private val options = BitmapFactory.Options()

/**
 * Created by PonyCui_Home on 16/6/18.
 */
class SVGAVideoEntity {

    var antiAlias = true

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

    constructor(obj: MovieEntity, cacheDir: File) {
        this.cacheDir = cacheDir
        obj.params?.let { movieParams ->
            videoSize = SVGARect(0.0, 0.0, (movieParams.viewBoxWidth ?: 0.0f).toDouble(), (movieParams.viewBoxHeight ?: 0.0f).toDouble())
            FPS = movieParams.fps ?: 20
            frames = movieParams.frames ?: 0
        }
        try {
            resetImages(obj)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        resetSprites(obj)
    }

    private fun resetImages(obj: JSONObject) {
        obj.optJSONObject("images")?.let { imgObjects ->
            imgObjects.keys().forEach { imageKey ->
                options.inPreferredConfig = Bitmap.Config.RGB_565
                var filePath = cacheDir.absolutePath + "/" + imgObjects[imageKey]
                var bitmap = if (File(filePath).exists()) BitmapFactory.decodeFile(filePath, options) else null
                if (bitmap != null) {
                    images.put(imageKey, bitmap)
                }
                else {
                    (cacheDir.absolutePath + "/" + imageKey + ".png")?.takeIf { File(it).exists() }?.let { it
                        BitmapFactory.decodeFile(it, options)?.let {
                            images.put(imageKey, it)
                        }
                    }
                }
            }
        }
    }

    private fun resetImages(obj: MovieEntity) {
        obj.images?.entries?.forEach {
            val imageKey = it.key
            options.inPreferredConfig = Bitmap.Config.RGB_565
            val bitmap = BitmapFactory.decodeByteArray(it.value.toByteArray(), 0, it.value.size(), options)
            if (bitmap != null) {
                images.put(imageKey, bitmap)
            }
            else {
                it.value.utf8()?.let {
                    var filePath = cacheDir.absolutePath + "/" + it
                    var bitmap = if (File(filePath).exists()) BitmapFactory.decodeFile(filePath, options) else null
                    if (bitmap != null) {
                        images.put(imageKey, bitmap)
                    }
                    else {
                        (cacheDir.absolutePath + "/" + imageKey + ".png")?.takeIf { File(it).exists() }?.let { it
                            BitmapFactory.decodeFile(it, options)?.let {
                                images.put(imageKey, it)
                            }
                        }
                    }
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

    private fun resetSprites(obj: MovieEntity) {
        sprites = obj.sprites?.map {
            return@map SVGAVideoSpriteEntity(it)
        } ?: listOf()
    }

}

