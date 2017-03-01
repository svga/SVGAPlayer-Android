package com.opensource.svgaplayer;

import android.graphics.Color;
import android.graphics.Matrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by cuiminghui on 2017/2/22.
 */

class SVGAVideoShapeEntity {

    enum Type {
        shape,
        rect,
        ellipse,
        keep,
    }

    class Styles {
        int fill = 0x00000000;
        int stroke = 0x00000000;
        float strokeWidth = 0.0f;
        String lineCap = "butt";
        String lineJoin = "miter";
        int miterLimit = 0;
        float[] lineDash = new float[0];
    }

    Type type = Type.shape;
    Map<String, Object> args = null;
    Styles styles = null;
    Matrix transform = null;

    SVGAVideoShapeEntity() {
        args = new HashMap<>();
        styles = new Styles();
        transform = new Matrix();
    }

    SVGAVideoShapeEntity(JSONObject object) {
        args = new HashMap<>();
        styles = new Styles();
        transform = new Matrix();
        fetchType(object);
        fetchArgs(object);
        fetchStyles(object);
        fetchTransform(object);
    }

    boolean isKeep() {
        return type == type.keep;
    }

    void fetchType(JSONObject object) {
        String value = object.optString("type");
        if (value.equalsIgnoreCase("shape")) {
            type = Type.shape;
        }
        else if (value.equalsIgnoreCase("rect")) {
            type = Type.rect;
        }
        else if (value.equalsIgnoreCase("ellipse")) {
            type = Type.ellipse;
        }
        else if (value.equalsIgnoreCase("keep")) {
            type = Type.keep;
        }
    }

    void fetchArgs(JSONObject object) {
        HashMap<String, Object> args = new HashMap<>();
        JSONObject values = object.optJSONObject("args");
        if (values != null) {
            Iterator<String> keys = values.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    Object value = values.get(key);
                    args.put(key, value);
                } catch (JSONException e) {
                    continue;
                }
            }
            this.args = args;
        }
    }

    void fetchStyles(JSONObject object) {
        JSONObject values = object.optJSONObject("styles");
        if (values != null) {
            JSONArray fillArr = values.optJSONArray("fill");
            if (fillArr != null && fillArr.length() == 4) {
                styles.fill = Color.argb((int)(fillArr.optDouble(3) * 255), (int)(fillArr.optDouble(0) * 255), (int)(fillArr.optDouble(1) * 255), (int)(fillArr.optDouble(2) * 255));
            }
            JSONArray strokeArr = values.optJSONArray("stroke");
            if (strokeArr != null && strokeArr.length() == 4) {
                styles.stroke = Color.argb((int)(strokeArr.optDouble(3) * 255), (int)(strokeArr.optDouble(0) * 255), (int)(strokeArr.optDouble(1) * 255), (int)(strokeArr.optDouble(2) * 255));
            }
            styles.strokeWidth = (float) values.optDouble("strokeWidth", 0.0);
            styles.lineCap = values.optString("lineCap", "butt");
            styles.lineJoin = values.optString("lineJoin", "miter");
            styles.miterLimit = values.optInt("miterLimit", 0);
            JSONArray lineDashArr = values.optJSONArray("lineDash");
            if (lineDashArr != null) {
                styles.lineDash = new float[lineDashArr.length()];
                for (int i = 0; i < lineDashArr.length(); i++) {
                    styles.lineDash[i] = (float) lineDashArr.optDouble(i, 0.0);
                }
            }
        }
    }

    void fetchTransform(JSONObject object) {
        JSONObject obj = object.optJSONObject("transform");
        if (obj != null) {
            final float[] arr = new float[9];
            double a = obj.optDouble("a", 1.0);
            double b = obj.optDouble("b", 0.0);
            double c = obj.optDouble("c", 0.0);
            double d = obj.optDouble("d", 1.0);
            double tx = obj.optDouble("tx", 0.0);
            double ty = obj.optDouble("ty", 0.0);
            arr[0] = (float) a; // a
            arr[1] = (float) c; // c
            arr[2] = (float) tx; // tx
            arr[3] = (float) b; // b
            arr[4] = (float) d; // d
            arr[5] = (float) ty; // ty
            arr[6] = (float) 0.0;
            arr[7] = (float) 0.0;
            arr[8] = (float) 1.0;
            this.transform.setValues(arr);
        }
    }

}
