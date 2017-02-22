package com.opensource.svgaplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by cuiminghui on 2016/10/17.
 */
class SVGAVideoSpriteEntity {

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
            SVGAVideoSpriteFrameEntity frameItem = new SVGAVideoSpriteFrameEntity(frameObject);
            if (frameItem.shapes.length == 1 && frameItem.shapes[0].isKeep() && frames.size() > 0) {
                frameItem.shapes = frames.get(frames.size() - 1).shapes;
            }
            frames.add(frameItem);
        }
    }

}
