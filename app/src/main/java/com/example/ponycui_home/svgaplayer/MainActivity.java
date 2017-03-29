package com.example.ponycui_home.svgaplayer;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;

import com.opensource.svgaplayer.SVGACanvasDrawer;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SVGAImageView testView = new SVGAImageView(this);
        testView.setBackgroundColor(Color.GRAY);
        SVGAParser parser = new SVGAParser(this);
        try {
            parser.parse(new URL("http://legox.yy.com/svga/svga-vector/PinJump.svga"), new SVGAParser.ParseCompletion() {
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
        } catch (Exception e) {}
        setContentView(testView);
    }
}
