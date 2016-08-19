package com.opensource.svgaplayer;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by PonyCui_Home on 16/6/18.
 */
public class SVGAVideoEntity implements Serializable {
    private static final String TAG = "SVGAVideoEntity";
    CGRect videoSize;
    int FPS;
    int frames;

    HashMap<String, String> imagePathMap;
    ArrayList<SVGAVideoSpriteEntity> sprites;
    File cacheDir;

    transient final HashMap<String, BitmapDrawable> images = new HashMap<>();
    transient final HashMap<BitmapCacheKey, Bitmap> bitmapCache = new HashMap<>();

    public SVGAVideoEntity() {
    }

    public SVGAVideoEntity(JSONObject obj, File cacheDir) throws JSONException {
        this.cacheDir = cacheDir;
        videoSize = new CGRect(0, 0, 100, 100);
        FPS = 20;
        sprites = new ArrayList<>();
        final JSONObject movie = obj.getJSONObject("movie");

        int width = movie.getJSONObject("viewBox").getInt("width");
        int height = movie.getJSONObject("viewBox").getInt("height");
        videoSize = new CGRect(0, 0, width, height);

        FPS = movie.getInt("fps");
        FPS = FPS <= 15 ? FPS : 15;

        frames = movie.getInt("frames");
    }

    void resetImages() {
        if (imagePathMap != null) {
            Set<Map.Entry<String, String>> set = imagePathMap.entrySet();

            for (Map.Entry<String, String> e : set) {
                BitmapDrawable bitmapDrawable = new BitmapDrawable(
                        Resources.getSystem(),
                        e.getValue());
                images.put(e.getKey(), bitmapDrawable);
            }

        }
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

class SVGAVideoSpriteEntity implements Serializable {

    String imageKey;
    ArrayList<SVGAVideoSpriteFrameEntity> frames;

    public SVGAVideoSpriteEntity() {
    }

    SVGAVideoSpriteEntity(JSONObject obj) throws JSONException {
        imageKey = obj.getString("imageKey");
        frames = new ArrayList<>();
        JSONArray jsonFrames = obj.getJSONArray("frames");
        for (int i = 0; i < jsonFrames.length(); i++) {
            JSONObject frameObject = jsonFrames.getJSONObject(i);
            frames.add(new SVGAVideoSpriteFrameEntity(frameObject));
        }
    }

}

class CGRect implements Serializable {

    double x = 0;
    double y = 0;
    double width = 0;
    double height = 0;

    public CGRect() {
    }

    CGRect(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

}

class SVGAVideoSpriteFrameEntity implements Serializable {

    double alpha;
    CGRect layout;
    final float[] arr = new float[9];
    private SVGAPath path;

    private transient final Matrix transform = new Matrix();
    private transient Path maskPath;

    public Matrix getTransform() {
        transform.setValues(arr);

        return transform;
    }

    public Path getMaskPath() {
        if (maskPath != null) {
            return maskPath;
        }
        if (path != null) {
            maskPath = path.getPath();
        }
        return maskPath;
    }

    public SVGAVideoSpriteFrameEntity() {

    }

    SVGAVideoSpriteFrameEntity(JSONObject obj) throws JSONException {
        alpha = obj.optDouble("alpha", 0);
        final JSONObject transformJO = obj.optJSONObject("transform");
        if (transformJO != null) {
            double a = transformJO.getDouble("a");
            double b = transformJO.getDouble("b");
            double c = transformJO.getDouble("c");
            double d = transformJO.getDouble("d");
            double tx = transformJO.getDouble("tx");
            double ty = transformJO.getDouble("ty");
            arr[0] = (float) a; // a
            arr[1] = (float) c; // c
            arr[2] = (float) tx; // tx
            arr[3] = (float) b; // b
            arr[4] = (float) d; // d
            arr[5] = (float) ty; // ty
            arr[6] = (float) 0.0;
            arr[7] = (float) 0.0;
            arr[8] = (float) 1.0;
        }

        final JSONObject layoutJO = obj.optJSONObject("layout");
        if (layoutJO != null) {
            double x = layoutJO.getDouble("x");
            double y = layoutJO.getDouble("y");
            double width = layoutJO.getDouble("width");
            double height = layoutJO.getDouble("height");
            layout = new CGRect(x, y, width, height);
        }

        String clipPath = obj.optString("clipPath");
        if (!TextUtils.isEmpty(clipPath)) {
            path = new SVGAPath();
            path.setValues(clipPath);
            maskPath = path.getPath();
        }
    }
}
