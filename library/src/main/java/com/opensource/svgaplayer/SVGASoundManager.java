package com.opensource.svgaplayer;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * Author : llk
 * Time : 2020/10/24
 * Description : svga音频加载管理类
 * 将SoundPool抽取到单例里边，规避load资源之后不回调onLoadComplete的问题。
 *
 * 相关文章：Android SoundPool崩溃问题研究
 * https://zhuanlan.zhihu.com/p/29985198
 */

public class SVGASoundManager {
    private static final String TAG = SVGASoundManager.class.getSimpleName();

    private SoundPool soundPool;

    private final Map<Integer, CompleteCallBack> completeCallBackMap = new HashMap<>();

    private SVGASoundManager() { }

    private static volatile SVGASoundManager instance;

    public static SVGASoundManager get() {
        if (instance == null) {
            synchronized (SVGASoundManager.class) {
                if (instance == null) {
                    instance = new SVGASoundManager();
                }
            }
        }
        return instance;
    }

    /**
     * 音频加载完成回调
     */
    interface CompleteCallBack{
        void onComplete();
    }

    public void init(){
        Log.d(TAG, "**************** init ****************");
        soundPool = getSoundPool();
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int soundId, int status) {
                Log.i(TAG, "SoundPool onLoadComplete soundId=" + soundId + " status=" + status);
                if (status == 0) { //加载该声音成功
                    if (completeCallBackMap.containsKey(soundId)){
                        CompleteCallBack cb = completeCallBackMap.get(soundId);
                        if (cb != null) cb.onComplete();
                    }
                }
            }
        });
    }

    public void release(){
        Log.d(TAG, "**************** release ****************");

        if (!completeCallBackMap.isEmpty()){
            completeCallBackMap.clear();
        }

        soundPool.release();
        soundPool = null;
    }

    private SoundPool getSoundPool(){
        if (Build.VERSION.SDK_INT >= 21){
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();
            return new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(20)
                    .build();
        }else {
            return new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
        }
    }

    public int load(CompleteCallBack callBack,
                    FileDescriptor fd,
                    long offset,
                    long length,
                    int priority){
        if (soundPool == null) {
            Log.e(TAG, "soundPool is null, you need call init() !!!");
            return -1;
        }

        int soundId = soundPool.load(fd, offset, length, priority);

        Log.i(TAG, "load soundId=" + soundId + " callBack=" + callBack);

        if (callBack != null
                && !completeCallBackMap.containsKey(soundId)){
            completeCallBackMap.put(soundId, callBack);
        }
        return soundId;
    }

    public void unload(int soundId){
        if (soundPool == null) {
            Log.e(TAG, "soundPool is null, you need call init() !!!");
            return;
        }

        Log.i(TAG, "unload soundId=" + soundId);

        soundPool.unload(soundId);
        completeCallBackMap.remove(soundId);
    }

    public int play(int soundId,
                     float leftVolume,
                     float rightVolume,
                     int priority,
                     int loop,
                     float rate){
        if (soundPool == null) {
            Log.e(TAG, "soundPool is null, you need call init() !!!");
            return -1;
        }

        Log.d(TAG, "play soundId=" + soundId);

        return soundPool.play(soundId, leftVolume, rightVolume, priority, loop, rate);
    }

    public void stop(int soundId){
        if (soundPool == null) {
            Log.e(TAG, "soundPool is null, you need call init() !!!");
            return;
        }

        Log.d(TAG, "stop soundId=" + soundId);

        soundPool.stop(soundId);
    }

    public void resume(int soundId){
        if (soundPool == null) {
            Log.e(TAG, "soundPool is null, you need call init() !!!");
            return;
        }

        Log.d(TAG, "stop soundId=" + soundId);

        soundPool.resume(soundId);
    }

    public void pause(int soundId){
        if (soundPool == null) {
            Log.e(TAG, "soundPool is null, you need call init() !!!");
            return;
        }

        Log.d(TAG, "pause soundId=" + soundId);

        soundPool.pause(soundId);
    }
}
