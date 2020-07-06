package com.opensource.svgaplayer

import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.opensource.svgaplayer.entities.SVGAAudioEntity
import com.opensource.svgaplayer.entities.SVGAVideoSpriteEntity
import com.opensource.svgaplayer.proto.MovieEntity
import com.opensource.svgaplayer.proto.MovieParams
import com.opensource.svgaplayer.utils.BitmapUtils
import com.opensource.svgaplayer.utils.SVGARect
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

/**
 * Created by PonyCui on 16/6/18.
 */
class SVGAVideoEntity {

    // FIXME: 2020/7/6 必须移除
    protected fun finalize() {
        this.soundPool?.release()
        this.soundPool = null
        this.images.clear()
    }

    var antiAlias = true
    var movieItem: MovieEntity? = null

    var videoSize = SVGARect(0.0, 0.0, 0.0, 0.0)
        private set

    var FPS = 15
        private set

    var frames: Int = 0
        private set

    internal var reqHeight = 0
    internal var reqWidth = 0
    internal var sprites: List<SVGAVideoSpriteEntity> = listOf()
    internal var audios: List<SVGAAudioEntity> = listOf()
    internal var soundPool: SoundPool? = null
    internal var images = HashMap<String, Bitmap>()
    private var mCacheDir: File

    constructor(json: JSONObject, cacheDir: File) {
        this.mCacheDir = cacheDir
        json.optJSONObject("movie")?.let(this::setup)
        try {
            parserImages(json)
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }
        resetSprites(json)
    }

    internal constructor(entity: MovieEntity, cacheDir: File) {
        this.movieItem = entity
        this.mCacheDir = cacheDir
        entity.params?.let (this::setup)

        try {
            parserImages(entity)
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }
        resetSprites(entity)
    }

    private fun setup(movieObject: JSONObject) {
        movieObject.optJSONObject("viewBox")?.let { viewBoxObject ->
            val width = viewBoxObject.optDouble("width", 0.0)
            val height = viewBoxObject.optDouble("height", 0.0)
            videoSize = SVGARect(0.0, 0.0, width, height)
        }
        FPS = movieObject.optInt("fps", 20)
        frames = movieObject.optInt("frames", 0)
    }

    private fun setup(movieParams: MovieParams) {
        val width = (movieParams.viewBoxWidth ?: 0.0f).toDouble()
        val height = (movieParams.viewBoxHeight ?: 0.0f).toDouble()
        videoSize = SVGARect(0.0, 0.0, width, height)
        FPS = movieParams.fps ?: 20
        frames = movieParams.frames ?: 0
    }

    internal fun prepare(callback: () -> Unit) {
        if (movieItem == null) {
            callback()
        } else {
            resetAudios(movieItem!!) {
                callback()
            }
        }
    }

    private fun parserImages(json: JSONObject) {
        val imgJson = json.optJSONObject("images") ?: return
        imgJson.keys().forEach { imgKey ->
            val filePath = generateBitmapFilePath(imgJson[imgKey].toString(), imgKey)
            if (filePath.isNotEmpty()) {
                val bitmapKey = imgKey.replace(".matte", "")
                val bitmap = createBitmap(filePath) ?: return@forEach
                images[bitmapKey] = bitmap
            }
        }
    }

    private fun generateBitmapFilePath(imgName: String, imgKey: String): String {
        val path = mCacheDir.absolutePath + "/" + imgName
        val path1 = "$path.png"
        val path2 = mCacheDir.absolutePath + "/" + imgKey + ".png"

        return when {
            File(path).exists() -> path
            File(path1).exists() -> path1
            File(path2).exists() -> path2
            else -> ""
        }
    }

    private fun parserImages(obj: MovieEntity) {
        obj.images?.entries?.forEach {
            val imageKey = it.key
            val byteArray = it.value.toByteArray()
            if (byteArray.count() < 4) {
                return@forEach
            }
            val fileTag = byteArray.slice(IntRange(0, 3))
            if (fileTag[0].toInt() == 73 && fileTag[1].toInt() == 68 && fileTag[2].toInt() == 51) {
            } else {
                val bitmap = BitmapUtils.decodeSampledBitmapFromByteArray(byteArray,reqWidth, reqHeight)
                if (bitmap != null) {
                    images[imageKey] = bitmap
                } else {
                    it.value.utf8()?.let {
                        var filePath = mCacheDir.absolutePath + "/" + it
                        var bitmap = if (File(filePath).exists()) createBitmap(filePath) else null
                        if (bitmap != null) {
                            images.put(imageKey, bitmap)
                        } else {
                            (mCacheDir.absolutePath + "/" + imageKey + ".png").takeIf { File(it).exists() }?.let {
                                createBitmap(it)?.let {
                                    images.put(imageKey, it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createBitmap(filePath: String): Bitmap? {
        return BitmapUtils.decodeSampledBitmapFromFile(filePath, reqWidth, reqHeight)
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

    private fun resetAudios(obj: MovieEntity, completionBlock: () -> Unit) {
        obj.audios?.takeIf { it.isNotEmpty() }?.let { audios ->
            var soundLoaded = 0
            val soundPool = if (android.os.Build.VERSION.SDK_INT >= 21) {
                SoundPool.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build())
                        .setMaxStreams(Math.min(12, audios.count()))
                        .build()
            } else {
                SoundPool(Math.min(12, audios.count()), AudioManager.STREAM_MUSIC, 0)
            }
            val audiosFile = HashMap<String, File>()
            soundPool.setOnLoadCompleteListener { _, _, _ ->
                soundLoaded++
                if (soundLoaded >= audios.count()) {
                    completionBlock()
                }
            }
            val audiosData = HashMap<String, ByteArray>()
            obj.images?.entries?.forEach {
                val imageKey = it.key
                val byteArray = it.value.toByteArray()
                if (byteArray.count() < 4) {
                    return@forEach
                }
                val fileTag = byteArray.slice(IntRange(0, 3))
                if (fileTag[0].toInt() == 73 && fileTag[1].toInt() == 68 && fileTag[2].toInt() == 51) {
                    audiosData[imageKey] = byteArray
                }
            }
            if (audiosData.count() > 0) {
                audiosData.forEach {
                    val tmpFile = File.createTempFile(it.key + "_tmp", ".mp3")
                    val fos = FileOutputStream(tmpFile)
                    fos.write(it.value)
                    fos.flush()
                    fos.close()
                    audiosFile[it.key] = tmpFile
                }
            }
            this.audios = audios.map { audio ->
                val item = SVGAAudioEntity(audio)
                audiosFile[audio.audioKey]?.let {
                    val fis = FileInputStream(it)
                    item.soundID = soundPool.load(fis.fd, (((audio.startTime
                            ?: 0).toDouble() / (audio.totalTime
                            ?: 0).toDouble()) * fis.available().toDouble()).toLong(), fis.available().toLong(), 1)
                    fis.close()
                }
                return@map item
            }
            this.soundPool = soundPool
        } ?: kotlin.run(completionBlock)
    }

}

