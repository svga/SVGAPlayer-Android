package com.example.ponycui_home.svgaplayer;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGACanvasDrawer;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by cuiminghui on 2017/3/30.
 * 这是最复杂的一个 Sample， 演示了从网络加载动画，并播放动画。
 * 更多的 Sample 可以在这里找到 https://github.com/yyued/SVGA-Samples
 */

public class MainActivity extends AppCompatActivity {

    SVGAImageView testView = null;
    SVGADynamicEntity dynamicItem = new SVGADynamicEntity();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        testView = new SVGAImageView(this);
        testView.setBackgroundColor(Color.GRAY);
        loadAnimation();
        setContentView(testView);
    }

    private void loadAnimation() {
        SVGAParser parser = new SVGAParser(this);
        resetDownloader(parser);
        try {
            parser.parse(new URL("https://github.com/yyued/SVGA-Samples/blob/master/angel.svga?raw=true"), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem);
                    testView.setImageDrawable(drawable);
                    testView.startAnimation();
                }
                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }
    }

    /**
     * 设置下载器，这是一个可选的配置项。
     * @param parser
     */
    private void resetDownloader(SVGAParser parser) {
        parser.setFileDownloader(new SVGAParser.FileDownloader() {
            @Override
            public void resume(final URL url, final Function1<? super InputStream, Unit> complete, final Function1<? super Exception, Unit> failure) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(url).get().build();
                        try {
                            Response response = client.newCall(request).execute();
                            complete.invoke(response.body().byteStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                            failure.invoke(e);
                        }
                    }
                }).start();
            }
        });
    }

}
