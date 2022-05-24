package com.opensource.svgaplayer

import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import com.opensource.svgaplayer.bitmap.SVGABitmapByteArrayDecoder
import com.opensource.svgaplayer.bitmap.SVGABitmapFileDecoder
import com.opensource.svgaplayer.entities.SVGAAudioEntity
import com.opensource.svgaplayer.entities.SVGAVideoSpriteEntity
import com.opensource.svgaplayer.proto.AudioEntity
import com.opensource.svgaplayer.proto.MovieEntity
import com.opensource.svgaplayer.proto.MovieParams
import com.opensource.svgaplayer.utils.SVGARect
import com.opensource.svgaplayer.utils.log.LogUtils
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by PonyCui on 16/6/18.
 */
class SVGAVideoEntity {

    private val TAG = "SVGAVideoEntity"

    var antiAlias = true

    var videoSize = SVGARect(0.0, 0.0, 0.0, 0.0)
        private set

    var FPS = 15
        private set

    var frames: Int = 0
        private set

    internal var spriteList: List<SVGAVideoSpriteEntity> = emptyList()
    internal var audioList: List<SVGAAudioEntity> = emptyList()
    internal var soundPool: SoundPool? = null
    private var soundCallback: SVGASoundManager.SVGASoundCallBack? = null
    internal var imageMap = HashMap<String, Bitmap>()
    private val bitmapsMap = mutableMapOf<String, Bitmap>()//图片文件路径:bitmap
    private var mCacheDir: File
    private var mFrameHeight = 0
    private var mFrameWidth = 0
    private var mPlayCallback: SVGAParser.PlayCallback?=null
    private lateinit var mCallback: () -> Unit

    constructor(json: JSONObject, cacheDir: File) : this(json, cacheDir, 0, 0)

    constructor(json: JSONObject, cacheDir: File, frameWidth: Int, frameHeight: Int) {
        mFrameWidth = frameWidth
        mFrameHeight = frameHeight
        mCacheDir = cacheDir
        val movieJsonObject = json.optJSONObject("movie") ?: return
        setupByJson(movieJsonObject)
        try {
            parserImages(json)
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }
        resetSprites(json)
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

    constructor(entity: MovieEntity, cacheDir: File) : this(entity, cacheDir, 0, 0)

    constructor(entity: MovieEntity, cacheDir: File, frameWidth: Int, frameHeight: Int) {
        this.mFrameWidth = frameWidth
        this.mFrameHeight = frameHeight
        this.mCacheDir = cacheDir
        entity.params?.let(this::setupByMovie)
        try {
            parserImages(entity)
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }
        resetSprites(entity)
    }

    private fun setupByMovie(movieParams: MovieParams) {
        val width = (movieParams.viewBoxWidth ?: 0.0f).toDouble()
        val height = (movieParams.viewBoxHeight ?: 0.0f).toDouble()
        videoSize = SVGARect(0.0, 0.0, width, height)
        FPS = movieParams.fps ?: 20
        frames = movieParams.frames ?: 0
    }

    internal fun prepare(movieItem: MovieEntity?,callback: () -> Unit, playCallback: SVGAParser.PlayCallback?) {
        mCallback = callback
        mPlayCallback = playCallback
        if (movieItem == null) {
            mCallback()
        } else {
            setupAudios(movieItem!!) {
                mCallback()
            }
        }
    }

    private fun parserImages(json: JSONObject) {
        val imgJson = json.optJSONObject("images") ?: return
        imgJson.keys().forEach { imgKey ->
            val filePath = generateBitmapFilePath(imgJson[imgKey].toString(), imgKey)
            if (filePath.isEmpty()) {
                return
            }
            val bitmapKey = imgKey.replace(".matte", "")
            val bitmap = createBitmap(filePath)
            if (bitmap != null) {
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
        // 问题：多个imageKey对应同一张图片时，这里却创建了多个bitmap，解压Rocket.svga查看可知
        // 这里的bitmapsMap就是为了避免重复创建相同的bitmap
        if (bitmapsMap.containsKey(filePath)) {
            return bitmapsMap[filePath]
        }
        return SVGABitmapFileDecoder.decodeBitmapFrom(filePath, mFrameWidth, mFrameHeight)?.apply {
            bitmapsMap[filePath] = this
        }
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
        val bitmap = SVGABitmapByteArrayDecoder.decodeBitmapFrom(byteArray, mFrameWidth, mFrameHeight)
        return bitmap ?: createBitmap(filePath)
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

    private var loadAudioCnt = 0
    private var setupAudioFinished = false

    private fun setupAudios(entity: MovieEntity, completionBlock: () -> Unit) {
        if (entity.audios == null || entity.audios.isEmpty()) {
            run(completionBlock)
            return
        }
        loadAudioCnt = 0
        setupAudioFinished = false
        //改成count，解决MovieEntity泄露问题
        setupSoundPool(entity.audios.count(), completionBlock)
        this.audioList = entity.audios.map { audio ->
            val item = SVGAAudioEntity(audio)
            val startTime = (audio.startTime ?: 0).toDouble()
            val totalTime = (audio.totalTime ?: 0).toDouble()
            if (totalTime.toInt() == 0) {
                // 除数不能为 0
                item
            } else {
                audio.audioKey?.let { imageKey ->
                    val audioCache = SVGACache.buildAudioFile(imageKey)
                    if (audioCache.exists()) {
                        //可能之前已经解过
                    } else {
                        entity.images[imageKey]?.toByteArray()?.apply {
                            if (count() < 4) return@apply
                            val fileTag = slice(IntRange(0, 3))
                            val isAudioData =
                                (fileTag[0].toInt() == 73 && fileTag[1].toInt() == 68 && fileTag[2].toInt() == 51) || (fileTag[0].toInt() == -1 && fileTag[1].toInt() == -5 && fileTag[2].toInt() == -108)
                            if (!isAudioData) return@apply
                            audioCache.parentFile?.mkdirs()
                            audioCache.createNewFile()
                            FileOutputStream(audioCache).write(this)
                        }
                    }
                    if (audioCache.exists()) {
                        FileInputStream(audioCache).use {
                            val length = it.available().toDouble()
                            val offset = ((startTime / totalTime) * length).toLong()
                            if (SVGASoundManager.isInit()) {
                                loadAudioCnt++
                                item.soundID = SVGASoundManager.load(
                                    soundCallback,
                                    it.fd,
                                    offset,
                                    length.toLong(),
                                    1
                                )
                            } else {
                                item.soundID = soundPool?.load(it.fd, offset, length.toLong(), 1)
                                if (soundPool != null) {
                                    loadAudioCnt++
                                }
                            }
                        }
                    }
                }
                item
            }
        }
        setupAudioFinished = true
        if (loadAudioCnt == 0) {
            completionBlock.invoke()
        }
    }

    private fun setupSoundPool(audioCount: Int, completionBlock: () -> Unit) {
        var soundLoaded = 0
        if (SVGASoundManager.isInit()) {
            soundCallback = object : SVGASoundManager.SVGASoundCallBack {
                override fun onVolumeChange(value: Float) {
                    SVGASoundManager.setVolume(value, this@SVGAVideoEntity)
                }

                override fun onComplete() {
                    soundLoaded++
                    if (setupAudioFinished && soundLoaded >= loadAudioCnt) {
                        completionBlock()
                    }
                }
            }
            return
        }
        soundPool = generateSoundPool(audioCount)
        LogUtils.info("SVGAParser", "pool_start")
        soundPool?.setOnLoadCompleteListener { _, _, _ ->
            LogUtils.info("SVGAParser", "pool_complete")
            soundLoaded++
            if (setupAudioFinished && soundLoaded >= loadAudioCnt) {
                completionBlock()
            }
        }
    }

    private fun generateSoundPool(audioCount: Int): SoundPool? {
        return try {
            if (Build.VERSION.SDK_INT >= 21) {
                val attributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                SoundPool.Builder().setAudioAttributes(attributes)
                    .setMaxStreams(12.coerceAtMost(audioCount))
                    .build()
            } else {
                SoundPool(12.coerceAtMost(audioCount), AudioManager.STREAM_MUSIC, 0)
            }
        } catch (e: Exception) {
            LogUtils.error(TAG, e)
            null
        }
    }

    fun clear() {
        if (SVGASoundManager.isInit()) {
            this.audioList.forEach {
                it.soundID?.let { id -> SVGASoundManager.unload(id) }
            }
            soundCallback = null
        }
        soundPool?.release()
        soundPool = null
        audioList = emptyList()
        spriteList = emptyList()
        bitmapsMap.clear()
        imageMap.clear()
    }
}

