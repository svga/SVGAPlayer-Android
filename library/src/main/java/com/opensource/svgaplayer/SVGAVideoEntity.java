package com.opensource.svgaplayer;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by PonyCui_Home on 16/6/18.
 */
public class SVGAVideoEntity {

    CGRect videoSize;
    int FPS;
    int frames;
    HashMap<String, BitmapDrawable> images;
    HashMap<String, Bitmap> bitmapCache = new HashMap<>();
    ArrayList<SVGAVideoSpriteEntity> sprites;
    File cacheDir;

    SVGAVideoEntity(JSONObject obj, File cacheDir) {
        this.cacheDir = cacheDir;
        videoSize = new CGRect(0, 0, 100, 100);
        FPS = 20;
        images = new HashMap<>();
        sprites = new ArrayList<>();
        try {
            int width = obj.getJSONObject("movie").getJSONObject("viewBox").getInt("width");
            int height = obj.getJSONObject("movie").getJSONObject("viewBox").getInt("height");
            videoSize = new CGRect(0, 0, width, height);
        } catch (Exception e) {}
        try {
            FPS = obj.getJSONObject("movie").getInt("fps");
        } catch (Exception e) {}
        try {
            frames = obj.getJSONObject("movie").getInt("frames");
        } catch (Exception e) {}
    }

    void resetImages(JSONObject obj) {
        try {
            JSONObject imgObjects = obj.getJSONObject("images");
            Iterator<?> keys = imgObjects.keys();
            while (keys.hasNext()) {
                String key = (String)keys.next();
                try {
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(Resources.getSystem(), this.cacheDir.getAbsolutePath() + "/" + imgObjects.getString(key) + ".png");
                    images.put(key, bitmapDrawable);
                } catch (Exception e) {}
            }
        } catch (Exception e) {}
    }

    void resetSprites(JSONObject obj) {
        try {
            JSONArray spriteObjects = obj.getJSONArray("sprites");
            for (int i = 0; i < spriteObjects.length(); i++) {
                try {
                    JSONObject spriteObject = spriteObjects.getJSONObject(i);
                    sprites.add(new SVGAVideoSpriteEntity(spriteObject));
                } catch (Exception e) { }
            }
        } catch (Exception e) { }
    }

}

class SVGAVideoSpriteEntity {

    String imageKey;
    ArrayList<SVGAVideoSpriteFrameEntity> frames;

    SVGAVideoSpriteEntity(JSONObject obj) throws JSONException {
        try {
            imageKey = obj.getString("imageKey");
            frames = new ArrayList<>();
            JSONArray jsonFrames = obj.getJSONArray("frames");
            for (int i = 0; i < jsonFrames.length(); i++) {
                JSONObject frameObject = jsonFrames.getJSONObject(i);
                frames.add(new SVGAVideoSpriteFrameEntity(frameObject));
            }
        } catch (JSONException e) {
            throw e;
        }
    }

}

class CGRect {

    double x = 0;
    double y = 0;
    double width = 0;
    double height = 0;

    CGRect(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

}

class SVGAVideoSpriteFrameEntity {

    double alpha;
    Matrix transform;
    CGRect layout;

    SVGAVideoSpriteFrameEntity(JSONObject obj) {
        alpha = 0.0;
        transform = new Matrix();
        layout = new CGRect(0, 0, 0, 0);
        try {
            alpha = obj.getDouble("alpha");
        } catch (Exception e) {}
        try {
            double a = obj.getJSONObject("transform").getDouble("a");
            double b = obj.getJSONObject("transform").getDouble("b");
            double c = obj.getJSONObject("transform").getDouble("c");
            double d = obj.getJSONObject("transform").getDouble("d");
            double tx = obj.getJSONObject("transform").getDouble("tx");
            double ty = obj.getJSONObject("transform").getDouble("ty");
            float[] arr = new float[9];
            arr[0] = (float)a; // a
            arr[1] = (float)c; // c
            arr[2] = (float)tx; // tx
            arr[3] = (float)b; // b
            arr[4] = (float)d; // d
            arr[5] = (float)ty; // ty
            arr[6] = (float)0.0;
            arr[7] = (float)0.0;
            arr[8] = (float)1.0;
            transform.setValues(arr);
        } catch (Exception e) {}
        try {
            double x = obj.getJSONObject("layout").getDouble("x");
            double y = obj.getJSONObject("layout").getDouble("y");
            double width = obj.getJSONObject("layout").getDouble("width");
            double height = obj.getJSONObject("layout").getDouble("height");
            layout = new CGRect(x, y, width, height);
        } catch (Exception e) {}
    }

}
