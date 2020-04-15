package com.example.ponycui_home.svgaplayer.parseTest;

import android.content.Context;
import android.util.Log;

import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

/****
 * Project： SVGAPlayer-Android
 * Author：yangshun@yy.com
 * YY：909041099
 * Created：2020/4/15 15:07
 * Description：
 *
 *
 ****/
public class SvgaHelper {
    private static final String TAG = "SvgaHelper";


    public static void playAvatarSvga(final SVGAImageView svgaImageView,
                                      final int rawId, final int times, final SVGACallback mSVGACallback) {
        Log.d(TAG,
                "playAvatarSvga() called with: svgaImageView = [" + svgaImageView + "], rawId = [" +
                        rawId + "], times = [" + times + "], mSVGACallback = [" + mSVGACallback +
                        "]");

        final Context context = svgaImageView.getContext();
        SVGAParser parser = new SVGAParser(context);
        parser.parse(context.getResources().openRawResource(rawId),
                context.getResources().getResourceEntryName(rawId),
                new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NotNull SVGAVideoEntity videoItem) {


                        Log.d(TAG, "onComplete() called with: videoItem = [" + videoItem + "]");
                        final SVGADynamicEntity dynamicItem = new SVGADynamicEntity();
                        SVGADrawable drawable = new SVGADrawable(videoItem, dynamicItem);
                        svgaImageView.setImageDrawable(drawable);
                        svgaImageView.setClearsAfterStop(false);
                        svgaImageView.setFillMode(SVGAImageView.FillMode.Backward);
                        svgaImageView.setLoops(times);
                        svgaImageView.startAnimation();
                        if (mSVGACallback != null) {
                            svgaImageView.setCallback(mSVGACallback);
                        }
                    }

                    @Override
                    public void onError() {
                        Log.e(TAG, "onError() called");

                        if (null != mSVGACallback) {
                            mSVGACallback.onFinished();
                        }
                    }
                }, true);
    }
}
