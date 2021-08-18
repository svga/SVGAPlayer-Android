package com.example.ponycui_home.svgaplayer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGASoundManager;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.opensource.svgaplayer.utils.log.SVGALogger;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnimationFromAssetsActivity extends Activity {

    int currentIndex = 0;
    SVGAImageView animationView = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        animationView = new SVGAImageView(this);
        animationView.setBackgroundColor(Color.BLACK);
        animationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animationView.stepToFrame(currentIndex++, false);
            }
        });
        SVGALogger.INSTANCE.setLogEnabled(true);
        SVGASoundManager.INSTANCE.init();
        loadAnimation();
        setContentView(animationView);
    }

    private void loadAnimation() {
        SVGAParser svgaParser = SVGAParser.Companion.shareParser();
//        String name = this.randomSample();
        //asset jojo_audio.svga  cannot callback
        String name = "mp3_to_long.svga";
        Log.d("SVGA", "## name " + name);
        svgaParser.setFrameSize(100, 100);
        svgaParser.decodeFromAssets(name, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                Log.e("zzzz", "onComplete: ");
                animationView.setVideoItem(videoItem);
                animationView.stepToFrame(0, true);
            }

            @Override
            public void onError() {
                Log.e("zzzz", "onComplete: ");
            }

        }, null);
    }

    private ArrayList<String> samples = new ArrayList();

    private String randomSample() {
        if (samples.size() == 0) {
            samples.add("750x80.svga");
            samples.add("alarm.svga");
            samples.add("angel.svga");
            samples.add("Castle.svga");
            samples.add("EmptyState.svga");
            samples.add("Goddess.svga");
            samples.add("gradientBorder.svga");
            samples.add("heartbeat.svga");
            samples.add("matteBitmap.svga");
            samples.add("matteBitmap_1.x.svga");
            samples.add("matteRect.svga");
            samples.add("MerryChristmas.svga");
            samples.add("posche.svga");
            samples.add("Rocket.svga");
            samples.add("rose.svga");
            samples.add("rose_2.0.0.svga");
        }
        return samples.get((int) Math.floor(Math.random() * samples.size()));
    }

}
