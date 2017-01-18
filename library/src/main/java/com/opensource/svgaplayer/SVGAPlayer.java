package com.opensource.svgaplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.TextureView;

import java.util.HashMap;

/**
 * Created by PonyCui_Home on 16/6/19.
 */
public class SVGAPlayer extends TextureView implements TextureView.SurfaceTextureListener {

    /* loops count, defaults to 0, means infinite. */
    public int loops = 0;

    /* clear contents after animation stop. defaults to true. */
    public boolean clearsAfterStop = true;

    /* init via code */
    public SVGAPlayer(Context context) {
        super(context);
        init();
    }

    /* init via layout.xml */
    public SVGAPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /* init via layout.xml */
    public SVGAPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /* Configure Callback */
    public void setCallback(SVGAPlayerCallback callback) {
        this.callback = callback;
    }

    /* Set VideoItem, pause all animations if replacing. */
    public void setVideoItem(SVGAVideoEntity videoItem) {
        this.stopAnimation();
        this.videoItem = videoItem;
    }

    /* Must call after set video item. */
    /* Return False IF FAILED.*/
    public boolean startAnimation() {
        this.animating = true;
        if (this.videoItem != null && this.drawer == null && this.isAvailable()) {
            this.createDrawer();
            this.startDrawing();
            return true;
        }
        else {
            return false;
        }
    }

    /* Stop current animation. */
    public void stopAnimation() {
        this.animating = false;
        this.stopDrawing();
        if (null != callback) {
            callback.onPause(this);
        }
        this.releaseDrawer();
    }

    /* Replace an image for key. */
    public void setDynamicImage(BitmapDrawable drawable, String forKey) {
        if (null != drawable) {
            this.dynamicImages.put(forKey, drawable);
        }
    }

    /* Add Text to image with key */
    public void setDynamicText(String text, TextPaint textPaint, String forKey) {
        if (null != text) {
            this.dynamicTexts.put(forKey, text);
        }
        if (null != textPaint) {
            textPaint.setTextSize(textPaint.getTextSize() * getResources().getDisplayMetrics().scaledDensity);
            this.dynamicTextPaints.put(forKey, textPaint);
        }
    }

    /* Clear all dynamic items. */
    public void clearDynamicObjects() {
        this.dynamicImages.clear();
        this.dynamicTexts.clear();
        this.dynamicTextPaints.clear();
    }

    private static final String TAG = "SVGAPlayer";
    protected HashMap<String, BitmapDrawable> dynamicImages = new HashMap<>();
    protected HashMap<String, String> dynamicTexts = new HashMap<>();
    protected HashMap<String, TextPaint> dynamicTextPaints = new HashMap<>();
    protected SVGAPlayerCallback callback;
    protected boolean animating = false;
    protected Thread drawerThread;
    protected SVGADrawer drawer;
    protected SVGAVideoEntity videoItem;
    protected Object drawerLock = new Object();

    private void init() {
        this.createDrawer();
        this.setSurfaceTextureListener(this);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setOpaque(false);
    }

    protected void createDrawer() {
        if (null == this.videoItem) {
            return;
        }
        this.drawer = new SVGADrawer();
        this.drawer.videoItem = this.videoItem;
        this.drawer.textureView = this;
        this.drawer.videoWidth = (int) (getWidth() / getResources().getDisplayMetrics().scaledDensity);
        this.drawer.scaledDensity = getResources().getDisplayMetrics().scaledDensity;
    }

    protected void releaseDrawer() {
        if (null != drawer) {
            drawer.videoItem = null;
            drawer = null;
        }
    }

    protected void startDrawing() {
        if (null != this.drawer) {
            drawerThread = new Thread(this.drawer);
            drawerThread.start();
        }
    }

    protected void stopDrawing() {
        synchronized (SVGADrawer.mLock) {
            if (null != drawerThread) {
                drawerThread.interrupt();
                drawerThread = null;
            }
            if (null != drawer) {
                if (null != drawer.currentCanvas) {
                    drawer.currentCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    this.unlockCanvasAndPost(drawer.currentCanvas);
                    drawer.currentCanvas = null;
                }
                else {
                    Canvas canvas = lockCanvas();
                    if (canvas != null) {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (this.animating) {
            synchronized (drawerLock) {
                this.createDrawer();
                this.startDrawing();
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        synchronized (drawerLock) {
            if (null != this.drawer) {
                this.drawer.videoWidth = (int) (getWidth() / getResources().getDisplayMetrics().scaledDensity);
                this.drawer.scaledDensity = getResources().getDisplayMetrics().scaledDensity;
            }
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        synchronized (drawerLock) {
            this.stopAnimation();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        
    }

}

