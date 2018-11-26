package com.opensource.svgaplayer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.opensource.svgaplayer.proto.MovieEntity
import org.json.JSONObject
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
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

    var audios: List<SVGAAudioEntity> = listOf()

    var soundPool: SoundPool? = null

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

    var _movieItem: MovieEntity? = null

    constructor(obj: MovieEntity, cacheDir: File) {
        this._movieItem = obj
        this.cacheDir = cacheDir
        obj.params?.let { movieParams ->
            videoSize = SVGARect(0.0, 0.0, (movieParams.viewBoxWidth
                    ?: 0.0f).toDouble(), (movieParams.viewBoxHeight ?: 0.0f).toDouble())
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

    internal fun prepare(callback: () -> Unit) {
        this._movieItem?.let {
            resetAudios(it) {
                callback()
            }
        }
    }

    private fun resetImages(obj: JSONObject) {
        obj.optJSONObject("images")?.let { imgObjects ->
            imgObjects.keys().forEach { imageKey ->
                options.inPreferredConfig = Bitmap.Config.RGB_565
                var filePath = cacheDir.absolutePath + "/" + imgObjects[imageKey]
                var bitmap = if (File(filePath).exists()) BitmapFactory.decodeFile(filePath, options) else null
                if (bitmap != null) {
                    images.put(imageKey, bitmap)
                } else {
                    (cacheDir.absolutePath + "/" + imageKey + ".png").takeIf { File(it).exists() }?.let {
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
            val byteArray = it.value.toByteArray()
            if (byteArray.count() < 4) {
                return@forEach
            }
            val fileTag = byteArray.slice(IntRange(0, 3))
            if (fileTag[0].toInt() == 73 && fileTag[1].toInt() == 68 && fileTag[2].toInt() == 51 && fileTag[3].toInt() == 3) {
            } else {
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.count(), options)
                if (bitmap != null) {
                    images[imageKey] = bitmap
                } else {
                    it.value.utf8()?.let {
                        var filePath = cacheDir.absolutePath + "/" + it
                        var bitmap = if (File(filePath).exists()) BitmapFactory.decodeFile(filePath, options) else null
                        if (bitmap != null) {
                            images.put(imageKey, bitmap)
                        } else {
                            (cacheDir.absolutePath + "/" + imageKey + ".png").takeIf { File(it).exists() }?.let {
                                BitmapFactory.decodeFile(it, options)?.let {
                                    images.put(imageKey, it)
                                }
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

    private fun resetAudios(obj: MovieEntity, completionBlock: () -> Unit) {
        obj.audios?.takeIf { it.isNotEmpty() }?.let { audios ->
            var soundLoaded = 0
//            val soundPool = SoundPool(Math.min(12, audios.count()), AudioManager.STREAM_RING, 0)
            //5.0以上版本建议使用builder方式创建SoundPool，在9.0以下还未发现使用new SoundPool有什么问题，9.0很多机型继续使用new SoundPool方式部分room已无效。
            val soundPool = if (android.os.Build.VERSION.SDK_INT >= 21) {
                SoundPool.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build())
                        .setMaxStreams(Math.min(12, audios.count()))
                        .build()
            } else
                SoundPool(Math.min(12, audios.count()), AudioManager.STREAM_MUSIC, 0)

            val audiosFile = HashMap<String, File>()
            soundPool.setOnLoadCompleteListener { soundPool, _, _ ->
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
                if (fileTag[0].toInt() == 73 && fileTag[1].toInt() == 68 && fileTag[2].toInt() == 51 && fileTag[3].toInt() == 3) {
                    audiosData[imageKey] = byteArray
                }
            }
            if (audiosData.count() > 0) {

                audiosData.forEach {
                    val tmpFile = File.createTempFile(it.key, ".mp3")
                    val fos = FileOutputStream(tmpFile)
                    fos.write(it.value)
                    fos.flush()
                    fos.close()
                    audiosFile[it.key] = tmpFile
                }

                //Call requires API level 24 (current min is 14): java.util.HashMap#forEach
                /*  audiosData.forEach { aKey, bytes ->
                      val tmpFile = File.createTempFile(aKey, ".mp3")
                      val fos = FileOutputStream(tmpFile)
                      fos.write(bytes)
                      fos.flush()
                      fos.close()
                      audiosFile[aKey] = tmpFile
                  }*/
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

