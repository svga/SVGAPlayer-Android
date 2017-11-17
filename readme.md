# SVGAPlayer

[![](https://jitpack.io/v/yyued/SVGAPlayer-Android.svg)](https://jitpack.io/#yyued/SVGAPlayer-Android)

## Android Studio 3.0.0

We Recommend You Upgrade [Android Studio 3.0.0](https://developer.android.com/studio/index.html?hl=zh-cn).

If you want to run Sample Project on Android Studio 2.3.2, Download this [commit](https://github.com/yyued/SVGAPlayer-Android/archive/465812d2b94ecace62a7e8f6c8da5bc593d43f63.zip).

我们推荐你将 Android Studio 升级到 3.0.0 版本，示例工程不能在 2.3.2 中开启（除非，你自行修改 Gradle 配置）。

如果你要在 Android Studio 2.3.2 中运行示例工程, 下载这个 [commit](https://github.com/yyued/SVGAPlayer-Android/archive/465812d2b94ecace62a7e8f6c8da5bc593d43f63.zip).

## Version

### 2.0.3

Improve: SVGAPath parsing faster then before.

### 2.0.1

Let antiAlias defaults to true, add DrawFilter to Canvas.

Add isAnimating props to SVGAImageView.

### 2.0.0

Add SVGA-Format 2.0.0 support.

### 1.2.7

* add ScaleType support.
* bug-fix: crash on layout.xml, view removing.

### 1.2.6

* reuse Path, decrease GC trigger, improves vector animation performance.

## SVGA Format

* SVGA is an opensource animation library, develop by YY UED.
* SVGA base on SVG's concept, but not compatible to SVG.
* SVGA can play on iOS/Android/Web.

@see https://github.com/yyued/SVGA-Format

## Install

### Gradle 

add JitPack.io repo build.gradle
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

add dependency to build.gradle (Final Release https://jitpack.io/#yyued/SVGAPlayer-Android/ )
```
compile 'com.github.yyued:SVGAPlayer-Android:2.0.3'
```

## Usage

### Layout.xml

use layout.xml.

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.opensource.svgaplayer.SVGAImageView
        android:layout_height="match_parent"
        android:layout_width="match_parent"
        app:source="posche.svga"
        app:autoPlay="true"
        android:background="#000" />

</RelativeLayout>
```

* source - SVGA file path，relative assets directory。
* autoPlay - defaults to true，play after load automatically。
* loopCount - defaults to 0，Loop Count, 0 = Infinity Loop。
* clearsAfterStop - Clears Canvas After Animation Stop
* fillMode - defaults to Forward，optional Forward / Backward，fillMode = Forward，Animation will pause on last frame while finished，fillMode = Backward , Animation will pause on first frame.

### Code

Add SVGAImageView via code.

#### Init ImageView

```
SVGAImageView imageView = new SVGAImageView(this);
```

#### Init Parser & Load File

```
parser = new SVGAParser(this);
parser.parse(new URL("http://legox.yy.com/svga/svga-me/angel.svga"), new SVGAParser.ParseCompletion() {
    @Override
    public void onComplete(@NotNull SVGAVideoEntity videoItem) {
        SVGADrawable drawable = new SVGADrawable(videoItem);
        imageView.setImageDrawable(drawable);
        imageView.startAnimation();
    }
    @Override
    public void onError() {

    }
});
```

### Cache

Parser will not manage cache, you need to cache by yourself.

#### Install HttpResponseCache

Because SVGAParser depends URLConnection, and URLConnection uses HttpResponseCache.

Add following code to Application.java:onCreate is Okey to handle SVGA caches.

```kotlin
val cacheDir = File(context.applicationContext.cacheDir, "http")
HttpResponseCache.install(cacheDir, 1024 * 1024 * 128)
```

## API

### Properties Setter

* setLoops(int loops); - Loop Count，0 = Infinity Loop
* setClearsAfterStop(boolean clearsAfterStop); - Clears Canvas After Animation Stop
* setFillMode(FillMode fillMode); - Optional Forward / Backward，fillMode = Forward，Animation will pause on last frame while finished，fillMode = Backward , Animation will pause on first frame.
* setCallback(SVGAPlayerCallback callback) - SET Callbacks
* setVideoItem(SVGAVideoEntity videoItem) - SET animation instance

### Methods
* startAnimation() - Play Animation from 0 frame.
* pauseAnimation() - Pause Animation and keep on current frame.
* stopAnimation() - Stop Animation，Clears Canvas while clearsAfterStop == YES.
* stepToFrame(int frame, boolean andPlay) - Step to N frame, and then Play Animation if andPlay === true.
* stepToPercentage(float percentage, boolean andPlay) - Step to x%, and then Play Animation if andPlay === true.

### SVGAPlayerCallback

* void onPause() - Call after animation paused.
* void onFinished() - Call after animation finished.
* void onRepeat() - Call while animation repeat.
* void onStep(int frame, float percentage) - Call after animation play to specific frame.

## Dynamic Object

You may replace Image or Text dynamically. To do this, you need to create a SVGADynamicEntity instance. (SVGAPlayer 支持动态图像和动态文本，要添加动态图像和动态文本，你需要创建一个 SVGADynamicEntity 对象，并传入 SVGDrawable 初始化方法。)

```
SVGADynamicEntity dynamicItem = new SVGADynamicEntity();
SVGADrawable drawable = new SVGADrawable(videoItem, dynamicItem);
```

### Dynamic Image

You need to create a bitmap instance, use setDynamicImage method, to replace specific image. Ask your designer to provide imageKey(or unzip the svga file, find it).

```
dynamicItem.setDynamicImage(bitmap or url, "99");
```

### Dynamic Text

Use setDynamicText method, to add text on specific image. Ask your designer to provide imageKey(or unzip the svga file, find it).

```
TextPaint textPaint = new TextPaint();
textPaint.setTextSize(30);
textPaint.setFakeBoldText(true);
textPaint.setARGB(0xff, 0xff, 0xe0, 0xa4);
textPaint.setShadowLayer((float)1.0, (float)0.0, (float)1.0, Color.BLACK); // 各种配置
dynamicItem.setDynamicText("崔小姐不吃鱼 送了魔法奇缘", textPaint, "banner");
```
