package com.txl.glide.model;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.model.ModelLoader;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGASimpleParser;
import com.opensource.svgaplayer.drawer.SVGAAnimationDrawable;
import com.txl.glide.SVGALoadKey;
import com.txl.glide.SVGALoadKeySVGADecoder;

import java.io.InputStream;


/**
 * 扩展ModelLoader  利用现在已有的解码变换功能
 */

public abstract class SVGALoader<Model> implements ModelLoader<Model, SVGALoadKey> {
    private static final String TAG = "AudioAssetUriLoader";

    protected final ModelLoader<Model, InputStream> modelLoader;

    private static boolean isInit = false;

    //全局一处调用即可
    public static void init(Context context) {
        if (!isInit) {
            //调用这个目的在于 后续需要缓存svga 音频文件 要用到SVGACache  所以需要提前初始化一下
            new SVGAParser(context);
            StringSVGAModelLoader.Companion.init(context);
            UriSVGAModelLoader.Companion.init(context);
            MultiSVGAModelLoaderV2.Companion.init(context);
            SVGALoadKeyEncoder.Companion.init(context);
            SVGAStreamDecoder.Companion.init(context);
            Glide.get(context).getRegistry().append(Registry.BUCKET_BITMAP_DRAWABLE, SVGALoadKey.class, SVGAAnimationDrawable.class, new SVGALoadKeySVGADecoder(
                    new SVGASimpleParser(), context));
            isInit = true;
        }
    }

    public SVGALoader(ModelLoader<Model, InputStream> modelLoader) {
        this.modelLoader = modelLoader;
    }
}
