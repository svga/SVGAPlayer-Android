# SVGAPlayer

## 介绍

`SVGAPlayer` 是一个轻量的动画渲染库。你可以使用[工具](http://svga.io/designer.html)从 `Adobe Animate CC` 或者 `Adobe After Effects` 中导出动画文件，然后使用 `SVGAPlayer` 在移动设备上渲染并播放。

`SVGAPlayer-Android` 使用原生 Android Canvas 库渲染动画，为你提供高性能、低开销的动画体验。

如果你想要了解更多细节，请访问[官方网站](http://svga.io/)。

## 用法

我们在这里介绍 `SVGAPlayer-Android` 的用法。想要知道如何导出动画，点击[这里](http://svga.io/designer.html)。

### 使用 Gradle 安装

我们的 aar 包托管在 JitPack 上，你需要将 `JitPack.io` 仓库添加到工程 `build.gradle` 中。

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

然后，在应用 `build.gradle` 中添加依赖。

```
compile 'com.github.yyued:SVGAPlayer-Android:latest'
```

[![](https://jitpack.io/v/yyued/SVGAPlayer-Android.svg)](https://jitpack.io/#yyued/SVGAPlayer-Android)

### 遮罩支持
请参阅此处 [Dynamic · Matte Layer](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-%C2%B7-Matte-Layer)

### 混淆规则

```
-keep class com.squareup.wire.** { *; }
-keep class com.opensource.svgaplayer.proto.** { *; }
```

### 放置 svga 文件

SVGAPlayer 可以从本地 `assets` 目录，或者远端服务器上加载动画文件。

### 使用 XML

你可以使用 `layout.xml` 添加一个 `SVGAImageView`。

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

在 XML 中，允许定义以下这些标签：

#### source: String
用于表示 svga 文件的路径，提供一个在 `assets` 目录下的文件名，或者提供一个 http url 地址。

#### autoPlay: Boolean
默认为 `true`，当动画加载完成后，自动播放。

#### loopCount: Int
默认为 `0`，设置动画的循环次数，0 表示无限循环。

#### clearsAfterStop: Boolean
默认为 `true`，当动画播放完成后，是否清空画布。

#### fillMode: String

默认为 `Forward`，可以是 `Forward`、 `Backward`。

`Forward` 表示动画结束后，将停留在最后一帧。

`Backward` 表示动画结束后，将停留在第一帧。

### 使用代码

也可以使用代码添加 `SVGAImageView`。

#### 创建一个 `SVGAImageView` 实例

```kotlin
SVGAImageView imageView = new SVGAImageView(this);
```

#### 创建一个 `SVGAParser` 实例，加载 assets 中的动画。

```kotlin
parser = new SVGAParser(this);
parser.decodeFromAssets("posche.svga", new SVGAParser.ParseCompletion() {
    
});
```

#### 创建一个 `SVGAParser` 实例，加载远端服务器中的动画。

```kotlin
parser = new SVGAParser(this);
parser.decodeFromURL(new URL("https://github.com/yyued/SVGA-Samples/blob/master/posche.svga?raw=true"), new SVGAParser.ParseCompletion() {
    
});
```

#### 创建一个 `SVGADrawable` 实例，并赋值给 `SVGAImageView`，然后播放动画。

```kotlin
parser = new SVGAParser(this);
parser.decodeFromURL(..., new SVGAParser.ParseCompletion() {
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

### 缓存

`SVGAParser` 不会管理缓存，你需要自行实现缓存器。

#### 设置 HttpResponseCache

`SVGAParser` 依赖 `URLConnection`, `URLConnection` 使用 `HttpResponseCache` 处理缓存。

添加代码至 `Application.java:onCreate` 以设置缓存。

```kotlin
val cacheDir = File(context.applicationContext.cacheDir, "http")
HttpResponseCache.install(cacheDir, 1024 * 1024 * 128)
```

## 功能示例

* [使用位图替换指定元素。](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Image)
* [在指定元素上绘制文本。](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Text)
* [在指定元素上绘制富文本。](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Text-Layout)
* [隐藏指定元素。](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Hidden)
* [在指定元素上自由绘制。](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Drawer)

## APIs

请参阅此处 [https://github.com/yyued/SVGAPlayer-Android/wiki/APIs](https://github.com/yyued/SVGAPlayer-Android/wiki/APIs)

## CHANGELOG

请参阅此处 [CHANGELOG](./CHANGELOG.md)