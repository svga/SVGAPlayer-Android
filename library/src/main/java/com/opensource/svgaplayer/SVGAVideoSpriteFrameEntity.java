package com.opensource.svgaplayer;

import android.graphics.Matrix;
import android.graphics.Path;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by cuiminghui on 2016/10/17.
 */
class SVGAVideoSpriteFrameEntity implements Serializable {

    double alpha;
    SVGARect layout;
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
            layout = new SVGARect(x, y, width, height);
        }

        String clipPath = obj.optString("clipPath");
        if (!TextUtils.isEmpty(clipPath)) {
            path = new SVGAPath();
            path.setValues(clipPath);
            maskPath = path.getPath();
        }
    }
}
