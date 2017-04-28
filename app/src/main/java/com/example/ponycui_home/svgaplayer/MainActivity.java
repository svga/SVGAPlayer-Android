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
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by cuiminghui on 2017/3/30.
 * 这是最复杂的一个 Sample， 演示了从网络加载动画，并播放动画。
 */

public class MainActivity extends AppCompatActivity {

    SVGAImageView testView = null;
    SVGADynamicEntity dynamicItem = new SVGADynamicEntity();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        testView = new SVGAImageView(this);
        testView.setBackgroundColor(Color.GRAY);
        setupCallback();
        loadAnimation();
        setContentView(testView);
    }

    private void setupCallback() {
        testView.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onRepeat() {

            }

            @Override
            public void onStep(int frame, double percentage) {
//                System.out.println("当前帧:" + frame);
//                System.out.println("当前百分比:" + percentage);
            }
        });
    }

    // 加载动态图像
    private void loadDynamicBitmap(final Runnable complete) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://img.hb.aicdn.com/80cc8e001ccdc54febd448dc45119b4bd7924ea5530b-RllWp3_sq320").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final Bitmap dynamicBitmap = BitmapFactory.decodeStream(response.body().byteStream());
                if (dynamicBitmap != null) {
                    final Bitmap editedBitmap = getRoundedCornerBitmap(dynamicBitmap, 168);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dynamicItem.setDynamicImage(editedBitmap, "99"); // 99 这个值是由设计提供的
                            complete.run();
                        }
                    });
                }
            }
        });
    }

    private void loadDynamicText() {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(30);
        textPaint.setFakeBoldText(true);
        textPaint.setARGB(0xff, 0xff, 0xe0, 0xa4);
        textPaint.setShadowLayer((float)1.0, (float)0.0, (float)1.0, Color.BLACK);
        dynamicItem.setDynamicText("崔小姐不吃鱼 送了魔法奇缘", textPaint, "banner");
    }

    // 如果有需要，你需要为动态加载的图像自行裁剪圆角、添加滤镜等操作。
    private static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    private void loadAnimation() {
        SVGAParser parser = new SVGAParser(this);
        try {
            parser.parse(new URL("http://legox.yy.com/svga/svga-me/angel.svga"), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem, dynamicItem);
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

}
