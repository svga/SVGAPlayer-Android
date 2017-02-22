package com.example.ponycui_home.svgaplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup;

import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAPlayer;
import com.opensource.svgaplayer.SVGAPlayerCallback;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    View backgroundView;
    OkHttpClient client = new OkHttpClient();
    protected Handler mUIHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        player = new SVGAPlayer(this);
        configureBackgroundView();
        addContentView(backgroundView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addContentView(player, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final MainActivity obj = this;
        backgroundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (obj.getIntent().getBooleanExtra("test", false)) {
                    finish();
                    return;
                }
                Intent intent = new Intent(obj, MainActivity.class);
                intent.putExtra("test", true);
                startActivityForResult(intent, RESULT_OK);
            }
        });
//        player.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       configurePlayer();
//                        configureDynamicPlayer();
                   }
               });
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.player != null) {
            this.player.stopAnimation();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.player != null) {
            this.player.stopAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.player != null) {
            this.player.stopAnimation();
        }
    }

    void configureBackgroundView() {
        backgroundView = new View(this);
        backgroundView.setBackgroundColor(Color.GRAY);
    }

    String[] giftUrls = new String[]{
            "http://legox.yy.com/svga/svga-me/angel.svga",
            "http://legox.yy.com/svga/svga-me/dice_1.svga",
            "http://legox.yy.com/svga/svga-me/dice_2.svga",
            "http://legox.yy.com/svga/svga-me/dice_3.svga",
            "http://legox.yy.com/svga/svga-me/dice_4.svga",
            "http://legox.yy.com/svga/svga-me/dice_5.svga",
            "http://legox.yy.com/svga/svga-me/dice_6.svga",
            "http://legox.yy.com/svga/svga-me/firework.svga",
            "http://legox.yy.com/svga/svga-me/kingset.svga",
            "http://legox.yy.com/svga/svga-me/kingset_dyn.svga",
            "http://legox.yy.com/svga/svga-me/posche.svga",
            "http://legox.yy.com/svga/svga-me/rainbowrose.svga",
            "http://legox.yy.com/svga/svga-me/rose.svga",

    };

    String[] giftUrls2 = new String[] {
            "http://legox.yy.com/svga/svga-zhiniu/boom.svga",
            "http://legox.yy.com/svga/svga-zhiniu/bs.svga",
            "http://legox.yy.com/svga/svga-zhiniu/caishen.svga",
            "http://legox.yy.com/svga/svga-zhiniu/fuduji.svga",
            "http://legox.yy.com/svga/svga-zhiniu/shit.svga",
            "http://legox.yy.com/svga/svga-zhiniu/snake.svga",
            "http://legox.yy.com/svga/svga-zhiniu/yanhua.svga"
    };

    String[] vectorUrls = new String[] {
            "http://legox.yy.com/svga/svga-vector/EmptyState.svga",
            "http://legox.yy.com/svga/svga-vector/HamburgerArrow.svga",
            "http://legox.yy.com/svga/svga-vector/PinJump.svga",
            "http://legox.yy.com/svga/svga-vector/TwitterHeart.svga",
            "http://legox.yy.com/svga/svga-vector/Walkthrough.svga"
    };

    SVGAParser parser;
    SVGAPlayer player;
    int i = 0;
    void configurePlayer() {
        player.loops = 1;
        player.clearsAfterStop = true;
        if (parser == null) {
            parser = new SVGAParser(this);
        }
        handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = vectorUrls[i++ % vectorUrls.length];
                    final SVGAVideoEntity videoItem = parser.parse(new URL(url));
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            player.setVideoItem(videoItem);
                            player.startAnimation();

                        }
                    });
                    mUIHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            player.stopAnimation();
                            configurePlayer();
                        }
                    }, 10000);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    void configureDynamicPlayer() {
        if (player == null) {
            player = new SVGAPlayer(this);
        }
        player.loops = 0;
        player.clearsAfterStop = true;
        final Handler handler = new Handler();
        final SVGAParser parser = new SVGAParser(this);
        final Request request = new Request.Builder().url("http://img.hb.aicdn.com/80cc8e001ccdc54febd448dc45119b4bd7924ea5530b-RllWp3_sq320").build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                BitmapDrawable bitmapDrawable = new BitmapDrawable(response.body().byteStream());
                if (null == bitmapDrawable) {
                    return;
                }
                bitmapDrawable = new BitmapDrawable(MainActivity.getRoundedCornerBitmap(bitmapDrawable.getBitmap(), 168));
                if (null != bitmapDrawable) {
                    player.setDynamicImage(bitmapDrawable, "99");
                    TextPaint textPaint = new TextPaint();
                    textPaint.setTextSize(15);
                    textPaint.setFakeBoldText(true);
                    textPaint.setARGB(0xff, 0xff, 0xe0, 0xa4);
                    textPaint.setShadowLayer((float)1.0, (float)0.0, (float)1.0, Color.BLACK);
                    player.setDynamicText("崔小姐不吃鱼 送了魔法奇缘", textPaint, "banner");
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final SVGAVideoEntity videoItem = parser.parse(new URL("http://uedfe.yypm.com/assets/svga-me/kingset_dyn.svga"));
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        player.setVideoItem(videoItem);
                                        player.startAnimation();
                                    }
                                });
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }
                    });
                    thread.start();
                }
            }
        });

    }

    // 你需要自行将 Image 处理成最终要展现的 Image，比如，添加圆角、添加边框等等。
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
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


    private static HandlerThread sHT;
    private static Handler sHandler;

    public synchronized static Handler handler() {
        if (sHT == null) {
            sHT = new HandlerThread("yycall-daemon");
            sHT.start();
        }

        if (sHandler == null) {
            sHandler = new Handler(sHT.getLooper());
        }

        return sHandler;
    }

}
