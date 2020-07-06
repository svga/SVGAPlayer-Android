package com.opensource.svgaplayer

import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import com.opensource.svgaplayer.entities.SVGAAudioEntity
import com.opensource.svgaplayer.entities.SVGAVideoSpriteEntity
import com.opensource.svgaplayer.proto.AudioEntity
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

    var antiAlias = true
    var movieItem: MovieEntity? = null

    var videoSize = SVGARect(0.0, 0.0, 0.0, 0.0)
        private set

    var FPS = 15
        private set

    var frames: Int = 0
        private set

    private var reqHeight = 0
    private var reqWidth = 0
    internal var spriteList: List<SVGAVideoSpriteEntity> = emptyList()
    internal var audioList: List<SVGAAudioEntity> = emptyList()
    internal var soundPool: SoundPool? = null
    internal var imageMap = HashMap<String, Bitmap>()
    private var mCacheDir: File
    private var mJsonMovie: JSONObject? = null

    constructor(json: JSONObject, cacheDir: File) {
        mJsonMovie = json
        mCacheDir = cacheDir
        json.optJSONObject("movie")?.let(this::setupByJson)
    }

    private fun setupByJson(movieObject: JSONObject) {
        movieObject.optJSONObject("viewBox")?.let { viewBoxObject ->
            val width = viewBoxObject.optDouble("width", 0.0)
            val height = viewBoxObject.optDouble("height", 0.0)
            videoSize = SVGARect(0.0, 0.0, width, height)
        }
        FPS = movieObject.optInt("fps", 20)
        frames = movieObject.optInt("frames", 0)
    }

    internal constructor(entity: MovieEntity, cacheDir: File) {
        this.movieItem = entity
        this.mCacheDir = cacheDir
        entity.params?.let (this::setupByMovie)
    }

    private fun setupByMovie(movieParams: MovieParams) {
        val width = (movieParams.viewBoxWidth ?: 0.0f).toDouble()
        val height = (movieParams.viewBoxHeight ?: 0.0f).toDouble()
        videoSize = SVGARect(0.0, 0.0, width, height)
        FPS = movieParams.fps ?: 20
        frames = movieParams.frames ?: 0
    }

    internal fun init(reqWidth:Int, reqHeight:Int) {
        this.reqWidth = reqWidth
        this.reqHeight = reqHeight
        if (mJsonMovie != null) {
            parsResourceByJson()
        } else if (movieItem != null) {
            parsResourceByMovie()
        }
    }

    private fun parsResourceByJson() {
        try {
            parserImages(mJsonMovie!!)
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }
        resetSprites(mJsonMovie!!)
    }

    private fun parsResourceByMovie() {
        try {
            parserImages(movieItem!!)
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }
        resetSprites(movieItem!!)
    }

    internal fun prepare(callback: () -> Unit) {
        if (movieItem == null) {
            callback()
        } else {
            setupAudios(movieItem!!) {
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
                imageMap[bitmapKey] = bitmap
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

    private fun createBitmap(filePath: String): Bitmap? {
        if (filePath.isEmpty()) {
            return null
        }
        return BitmapUtils.decodeSampledBitmapFromFile(filePath, reqWidth, reqHeight)
    }

    private fun parserImages(obj: MovieEntity) {
        obj.images?.entries?.forEach { entry ->
            val byteArray = entry.value.toByteArray()
            if (byteArray.count() < 4) {
                return@forEach
            }
            val fileTag = byteArray.slice(IntRange(0, 3))
            if (fileTag[0].toInt() == 73 && fileTag[1].toInt() == 68 && fileTag[2].toInt() == 51) {
                return@forEach
            }
            val filePath = generateBitmapFilePath(entry.value.utf8(), entry.key)
            createBitmap(byteArray, filePath)?.let { bitmap ->
                imageMap[entry.key] = bitmap
            }
        }
    }

    private fun createBitmap(byteArray: ByteArray, filePath: String): Bitmap? {
        return BitmapUtils.decodeSampledBitmapFromByteArray(byteArray, reqWidth, reqHeight) ?: createBitmap(filePath)
    }

    private fun resetSprites(json: JSONObject) {
        val mutableList: MutableList<SVGAVideoSpriteEntity> = mutableListOf()
        json.optJSONArray("sprites")?.let { item ->
            for (i in 0 until item.length()) {
                item.optJSONObject(i)?.let { entryJson ->
                    mutableList.add(SVGAVideoSpriteEntity(entryJson))
                }
            }
        }
        spriteList = mutableList.toList()
    }

    private fun resetSprites(entity: MovieEntity) {
        spriteList = entity.sprites?.map {
            return@map SVGAVideoSpriteEntity(it)
        } ?: listOf()
    }

    private fun setupAudios(entity: MovieEntity, completionBlock: () -> Unit) {
        if (entity.audios == null || entity.audios.isEmpty()) {
            run(completionBlock)
        }
        setupSoundPool(entity, completionBlock)
        val audiosFileMap = generateAudioFileMap(entity)
        this.audioList = entity.audios.map { audio ->
            return@map createSvgaAudioEntity(audio, audiosFileMap)
        }
    }

    private fun createSvgaAudioEntity(audio: AudioEntity, audiosFileMap: HashMap<String, File>): SVGAAudioEntity {
        val item = SVGAAudioEntity(audio)
        val startTime = (audio.startTime ?: 0).toDouble()
        val totalTime = (audio.totalTime ?: 0).toDouble()
        if (totalTime.toInt() == 0) {
            // 除数不能为 0
            return item
        }
        audiosFileMap[audio.audioKey]?.let {
            val fis = FileInputStream(it)
            val length = fis.available().toDouble()
            val offset = ((startTime / totalTime) * length).toLong()
            item.soundID = soundPool?.load(fis.fd, offset, length.toLong(), 1)
            fis.close()
        }
        return item
    }

    private fun generateAudioFileMap(entity: MovieEntity): HashMap<String, File> {
        val audiosDataMap = generateAudioMap(entity)
        val audiosFileMap = HashMap<String, File>()
        if (audiosDataMap.count() > 0) {
            audiosDataMap.forEach {
                val tmpFile = File.createTempFile(it.key + "_tmp", ".mp3")
                val fos = FileOutputStream(tmpFile)
                fos.write(it.value)
                fos.flush()
                fos.close()
                audiosFileMap[it.key] = tmpFile
            }
        }
        return audiosFileMap
    }

    private fun generateAudioMap(entity: MovieEntity): HashMap<String, ByteArray> {
        val audiosDataMap = HashMap<String, ByteArray>()
        entity.images?.entries?.forEach {
            val imageKey = it.key
            val byteArray = it.value.toByteArray()
            if (byteArray.count() < 4) {
                return@forEach
            }
            val fileTag = byteArray.slice(IntRange(0, 3))
            if (fileTag[0].toInt() == 73 && fileTag[1].toInt() == 68 && fileTag[2].toInt() == 51) {
                audiosDataMap[imageKey] = byteArray
            }
        }
        return audiosDataMap
    }

    private fun setupSoundPool(entity: MovieEntity, completionBlock: () -> Unit) {
        var soundLoaded = 0
        soundPool = generateSoundPool(entity);
        soundPool?.setOnLoadCompleteListener { _, _, _ ->
            soundLoaded++
            if (soundLoaded >= entity.audios.count()) {
                completionBlock()
            }
        }
    }

    private fun generateSoundPool(entity: MovieEntity) = if (Build.VERSION.SDK_INT >= 21) {
        val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        SoundPool.Builder().setAudioAttributes(attributes)
                .setMaxStreams(12.coerceAtMost(entity.audios.count()))
                .build()
    } else {
        SoundPool(12.coerceAtMost(entity.audios.count()), AudioManager.STREAM_MUSIC, 0)
    }

    internal fun clear() {
        soundPool?.release()
        soundPool = null
        audioList = emptyList()
        spriteList = emptyList()
        imageMap.clear()
    }
}

