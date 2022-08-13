package com.txl.glide.model;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.opensource.svgaplayer.SVGASimpleParser;
import com.opensource.svgaplayer.drawer.SVGAAnimationDrawable;
import com.txl.glide.SVGALoadKey;
import com.txl.glide.StreamSVGADecoder;

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
            StringSVGAModelLoader.Companion.init(context);
            UriSVGAModelLoader.Companion.init(context);
            MultiSVGAModelLoaderV2.Companion.init(context);
            SVGALoadKeyEncoder.Companion.init(context);
            SVGALoadKeyDecoder.Companion.init(context);
            Glide.get(context).getRegistry().append(Registry.BUCKET_BITMAP_DRAWABLE, SVGALoadKey.class, SVGAAnimationDrawable.class, new StreamSVGADecoder(
                    new SVGASimpleParser(), context));
            isInit = true;
        }
    }

    public SVGALoader(ModelLoader<Model, InputStream> modelLoader) {
        this.modelLoader = modelLoader;
    }
}
