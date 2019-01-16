package com.example.ponycui_home.svgaplayer;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by cuiminghui on 2017/3/30.
 * 将 svga 文件打包到 assets 文件夹中，然后使用 layout.xml 加载动画。
 */

public class AnimationFromLayoutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
    }

}
