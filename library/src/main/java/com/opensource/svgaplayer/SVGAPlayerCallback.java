package com.opensource.svgaplayer;

/**
 * Created by cuiminghui on 2016/10/17.
 */
public interface SVGAPlayerCallback {

    void onPause(SVGAPlayer svgaPlayer);

    void onFinished(SVGAPlayer svgaPlayer);

    void onStep(SVGAPlayer svgaPlayer, int frame, float percentage);

}
