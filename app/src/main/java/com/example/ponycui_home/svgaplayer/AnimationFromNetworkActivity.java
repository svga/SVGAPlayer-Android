package com.example.ponycui_home.svgaplayer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

public class AnimationFromNetworkActivity extends Activity {

    SVGAImageView animationView = null;
    SVGAParser parser = new SVGAParser(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        animationView = new SVGAImageView(this);
        animationView.setBackgroundColor(Color.GRAY);
        loadAnimation();
        setContentView(animationView);
    }

    private void loadAnimation() {
        try { // new URL needs try catch.
            parser.decodeFromURL(new URL("https://github.com/yyued/SVGA-Samples/blob/master/posche.svga?raw=true"), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    animationView.setVideoItem(videoItem);
                    animationView.startAnimation();
                }
                @Override
                public void onError() {

                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
