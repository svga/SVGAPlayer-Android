package com.opensource.svgaplayer

/**
 * @author Devin
 *
 * Created on 2/24/21.
 */
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import com.opensource.svgaplayer.utils.log.LogUtils
import java.io.FileDescriptor

/**
 * Author : llk
 * Time : 2020/10/24
 * Description : svga 音频加载管理类
 * 将 SoundPool 抽取到单例里边，规避 load 资源之后不回调 onLoadComplete 的问题。
 *
 * 需要对 SVGASoundManager 进行初始化
 *
 * 相关文章：Android SoundPool 崩溃问题研究
 * https://zhuanlan.zhihu.com/p/29985198
 */
object SVGASoundManager {

    private val TAG = SVGASoundManager::class.java.simpleName

    private var soundPool: SoundPool? = null

    private val completeCallBackMap: MutableMap<Int, CompleteCallBack> = mutableMapOf()

    private object SingletonHolder{
        val holder = SVGASoundManager()
    }

    companion object{
        fun get() = SingletonHolder.holder
    }

    /**
     * 音频加载完成回调
     */
    interface CompleteCallBack {
        fun onComplete()
    }

    fun init() {
        init(20)
    }

    fun init(maxStreams: Int) {
        LogUtils.debug(TAG, "**************** init **************** $maxStreams")
        if (soundPool != null) {
            return
        }
        soundPool = getSoundPool(maxStreams)
        soundPool?.setOnLoadCompleteListener { _, soundId, status ->
            LogUtils.debug(TAG, "SoundPool onLoadComplete soundId=$soundId status=$status")
            if (status == 0) { //加载该声音成功
                if (completeCallBackMap.containsKey(soundId)) {
                    completeCallBackMap[soundId]?.onComplete()
                }
            }
        }
    }

    fun release() {
        LogUtils.debug(TAG, "**************** release ****************")
        if (completeCallBackMap.isNotEmpty()){
            completeCallBackMap.clear()
        }
    }

    /**
     * 是否初始化
     * 如果没有初始化，就使用原来SvgaPlayer库的音频加载逻辑。
     * @return true 则已初始化， 否则为 false
     */
    internal fun isInit(): Boolean {
        return soundPool != null
    }

    private fun checkInit(): Boolean {
        val isInit = isInit()
        if (!isInit) {
            LogUtils.error(TAG, "soundPool is null, you need call init() !!!")
        }
        return isInit
    }

    private fun getSoundPool(maxStreams: Int) = if (Build.VERSION.SDK_INT >= 21) {
        val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        SoundPool.Builder().setAudioAttributes(attributes)
                .setMaxStreams(maxStreams)
                .build()
    } else {
        SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0)
    }

    fun load(callBack: CompleteCallBack?,
             fd: FileDescriptor?,
             offset: Long,
             length: Long,
             priority: Int): Int {
        if (!checkInit()) return -1

        val soundId = soundPool!!.load(fd, offset, length, priority)

        LogUtils.debug(TAG, "load soundId=$soundId callBack=$callBack")

        if (callBack != null && !completeCallBackMap.containsKey(soundId)) {
            completeCallBackMap[soundId] = callBack
        }
        return soundId
    }

    internal fun unload(soundId: Int) {
        if (!checkInit()) return

        LogUtils.debug(TAG, "unload soundId=$soundId")

        soundPool!!.unload(soundId)

        completeCallBackMap.remove(soundId)
    }

    fun play(soundId: Int,
             leftVolume: Float,
             rightVolume: Float,
             priority: Int,
             loop: Int,
             rate: Float): Int {
        if (!checkInit()) return -1

        LogUtils.debug(TAG, "play soundId=$soundId")
        return soundPool!!.play(soundId, leftVolume, rightVolume, priority, loop, rate)
    }

    internal fun stop(soundId: Int) {
        if (!checkInit()) return

        LogUtils.debug(TAG, "stop soundId=$soundId")
        soundPool!!.stop(soundId)
    }

    internal fun resume(soundId: Int) {
        if (!checkInit()) return

        LogUtils.debug(TAG, "stop soundId=$soundId")
        soundPool!!.resume(soundId)
    }

    internal fun pause(soundId: Int) {
        if (!checkInit()) return

        LogUtils.debug(TAG, "pause soundId=$soundId")
        soundPool!!.pause(soundId)
    }
}