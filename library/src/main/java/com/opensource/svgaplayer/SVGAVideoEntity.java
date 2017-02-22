package com.opensource.svgaplayer;

import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by PonyCui_Home on 16/6/18.
 */
public class SVGAVideoEntity {

    private static final String TAG = "SVGAVideoEntity";
    SVGARect videoSize = new SVGARect(0, 0, 0, 0);
    int FPS = 15;
    int frames;

    HashMap<String, String> imagePathMap;
    ArrayList<SVGAVideoSpriteEntity> sprites;
    File cacheDir;

    final HashMap<String, BitmapDrawable> images = new HashMap<>();

    public SVGAVideoEntity(JSONObject obj, File cacheDir) throws JSONException {
        this.cacheDir = cacheDir;
        sprites = new ArrayList<>();
        final JSONObject movie = obj.getJSONObject("movie");
        int width = movie.getJSONObject("viewBox").getInt("width");
        int height = movie.getJSONObject("viewBox").getInt("height");
        videoSize = new SVGARect(0, 0, width, height);
        FPS = movie.getInt("fps");
        frames = movie.getInt("frames");
    }

    void resetImages(JSONObject obj) {
        try {
            JSONObject imgObjects = obj.getJSONObject("images");
            Iterator<?> keys = imgObjects.keys();
            imagePathMap = new HashMap<>();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                try {
                    final String path = this.cacheDir.getAbsolutePath() + "/" + imgObjects.getString(key) + ".png";
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(
                            Resources.getSystem(),
                            path);
                    images.put(key, bitmapDrawable);
                    imagePathMap.put(key, path);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
    }

    void resetSprites(JSONObject obj) {
        try {
            JSONArray spriteObjects = obj.getJSONArray("sprites");
            for (int i = 0; i < spriteObjects.length(); i++) {
                try {
                    JSONObject spriteObject = spriteObjects.getJSONObject(i);
                    sprites.add(new SVGAVideoSpriteEntity(spriteObject));
                } catch (Exception e) {
                    Log.e(TAG, "create SVGAVideoSpriteEntity failed.", e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "resetSprites failed.", e);
        }
    }

}

