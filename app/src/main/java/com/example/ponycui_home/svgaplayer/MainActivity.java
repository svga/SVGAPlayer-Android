package com.example.ponycui_home.svgaplayer;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAPlayer;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        SVGAPlayer player = new SVGAPlayer(this);
        player.setVideoWidth((int)(getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().scaledDensity));
        player.loops = 0;
        player.clearsAfterStop = true;
        player.setBackgroundColor(Color.BLACK);
        setContentView(player);

        try {
            InputStream inputStream = this.getAssets().open("test2.svga");
            SVGAParser parser = new SVGAParser();
            try {
                player.setVideoItem(parser.parse(inputStream));
                player.startAnimation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
