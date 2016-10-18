package com.opensource.svgaplayer;

import java.io.Serializable;

/**
 * Created by cuiminghui on 2016/10/17.
 */
class SVGARect implements Serializable {

    double x = 0;
    double y = 0;
    double width = 0;
    double height = 0;

    public SVGARect() {
    }

    SVGARect(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

}
