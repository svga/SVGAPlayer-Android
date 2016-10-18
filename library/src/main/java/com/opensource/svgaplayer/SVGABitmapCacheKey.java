package com.opensource.svgaplayer;

import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Created by ruoshili on 7/18/2016.
 */
class SVGABitmapCacheKey {
    @NonNull
    final String bitmapKey;
    final int width, height;

    public SVGABitmapCacheKey(String bitmapKey, int width, int height) {
        if (TextUtils.isEmpty(bitmapKey)) {
            throw new NullPointerException("bitmapKey is empty.");
        }

        this.bitmapKey = bitmapKey;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SVGABitmapCacheKey that = (SVGABitmapCacheKey) o;

        if (width != that.width) return false;
        if (height != that.height) return false;
        return bitmapKey.equals(that.bitmapKey);

    }

    @Override
    public int hashCode() {
        int result = bitmapKey.hashCode();
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }
}
