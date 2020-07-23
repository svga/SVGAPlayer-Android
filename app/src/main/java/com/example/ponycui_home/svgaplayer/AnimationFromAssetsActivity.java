package com.example.ponycui_home.svgaplayer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
        loadAnimation();
        setContentView(animationView);
    }

    private void loadAnimation() {
        SVGAParser svgaParser = SVGAParser.Companion.shareParser();
        String name = this.randomSample();
        Log.d("SVGA", "## name " + name);
        svgaParser.setFrameSize(100, 100);
        svgaParser.decodeFromAssets(name, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                animationView.setVideoItem(videoItem);
                animationView.stepToFrame(0, true);
            }

            @Override
            public void onError() {

            }
        });
    }

    private ArrayList<String> samples = new ArrayList();

    private String randomSample() {
        if (samples.size() == 0) {
            samples.add("gradientBorder.svga");
            samples.add("Goddess.svga");
            samples.add("Rocket.svga");
            samples.add("angel.svga");
            samples.add("alarm.svga");
            samples.add("EmptyState.svga");
            samples.add("heartbeat.svga");
            samples.add("posche.svga");
            samples.add("rose_2.0.0.svga");
            samples.add("Castle.svga");
            samples.add("MerryChristmas.svga");
        }
        return samples.get((int) Math.floor(Math.random() * samples.size()));
    }

}
