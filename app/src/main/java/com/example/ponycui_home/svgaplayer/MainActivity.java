package com.example.ponycui_home.svgaplayer;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAPlayer;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.net.URL;

public class MainActivity extends AppCompatActivity {

    View backgroundView;
    SVGAPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureBackgroundView();
        configurePlayer();
        addContentView(backgroundView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        FrameLayout frameLayout = new FrameLayout(this);
//        frameLayout.addView(player, 400, 400);
//        frameLayout.setBackgroundColor(Color.TRANSPARENT);
        addContentView(player, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    void configureBackgroundView() {
        backgroundView = new View(this);
        backgroundView.setBackgroundColor(Color.GRAY);
    }

    void configurePlayer() {
        player = new SVGAPlayer(this);
        player.loops = 0;
        player.clearsAfterStop = true;
        final Handler handler = new Handler();
        final SVGAParser parser = new SVGAParser(this);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final SVGAVideoEntity videoItem = parser.parse(new URL("http://uedfe.yypm.com/assets/svga-me/rose.svga"));
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
