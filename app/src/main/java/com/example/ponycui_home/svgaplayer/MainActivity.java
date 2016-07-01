package com.example.ponycui_home.svgaplayer;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAPlayer;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SVGAPlayer player = new SVGAPlayer(this);
        player.loops = 0;
        player.clearsAfterStop = true;
        setContentView(player);

//        try {
//            InputStream inputStream = this.getAssets().open("angel.svga");
//            SVGAParser parser = new SVGAParser(this);
//            try {
//                SVGAVideoEntity videoItem = parser.parse(inputStream, "angel");
//                player.setVideoItem(videoItem);
//                player.startAnimation();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        final Handler handler = new Handler();
        final SVGAParser parser = new SVGAParser(this);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final SVGAVideoEntity videoItem = parser.parse(new URL("http://uedfe.yypm.com/assets/svga-me/angel.svga"));
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
