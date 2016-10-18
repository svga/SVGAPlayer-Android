package com.opensource.svgaplayer;

import java.io.Serializable;

/**
 * Created by cuiminghui on 16/6/28.
 */

class SVGAPoint implements Serializable {

    float x;
    float y;
    float val;

    SVGAPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    SVGAPoint(float val) {
        this.val = val;
    }

}
