# SVGAPlayer

[简体中文](./readme.zh.md)

## 支持本项目

SVGA 是 PonyCui 在 2016 年于欢聚时代开发的一个跨平台动画库，至 2021 年，已帮助数百个 APP 高效、稳定实现动画播放。但长期以来，各 Player 的 issue 跟进并不及时，现在各个仓库的维护均由开发者们自愿进行，这不利于 SVGA 生态发展。

在此，作者呼吁，如果 SVGA 为您提供了便利与帮助，诚恳建议您通过以下方式支持作者、贡献者持续为该项目发电。

1. 轻点 GitHub Star，让更多人看到该项目。
2. 通过 [爱发电](https://afdian.net/@ponycui/plan) 月度捐赠的方式支持作者持续维护该仓库。
3. 如果您需要更深度的技术支持服务，也可以通过上述爱发电平台，成为银牌、金牌会员，作者将为您提供单独的顾问服务。

## Introduce

SVGAPlayer is a light-weight animation renderer. You use [tools](http://svga.io/designer.html) to export `svga` file from `Adobe Animate CC` or `Adobe After Effects`, and then use SVGAPlayer to render animation on mobile application.

`SVGAPlayer-Android` render animation natively via Android Canvas Library, brings you a high-performance, low-cost animation experience.

If wonder more information, go to this [website](http://svga.io/).

## Usage

Here introduce `SVGAPlayer-Android` usage. Wonder exporting usage? Click [here](http://svga.io/designer.html).

### Install Via Gradle

We host aar file on JitPack, your need to add `JitPack.io` repo `build.gradle`

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Then, add dependency to app `build.gradle`.

```
compile 'com.github.yyued:SVGAPlayer-Android:latest'
```

[![](https://jitpack.io/v/yyued/SVGAPlayer-Android.svg)](https://jitpack.io/#yyued/SVGAPlayer-Android)

### Static Parser Support
Perser#shareParser should be init(context) in Application or other Activity.
Otherwise it will report an error:
`Log.e("SVGAParser", "在配置 SVGAParser context 前, 无法解析 SVGA 文件。")`


### Matte Support
Head on over to [Dynamic · Matte Layer](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-%C2%B7-Matte-Layer)

### Proguard-rules

```
-keep class com.squareup.wire.** { *; }
-keep class com.opensource.svgaplayer.proto.** { *; }
```

### Locate files

SVGAPlayer could load svga file from Android `assets` directory or remote server.

### Using XML

You may use `layout.xml` to add a `SVGAImageView`.

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

The following attributes is allowable:

#### source: String

The svga file path, provide a path relative to Android assets directory, or provide a http url.

#### autoPlay: Boolean

Defaults to `true`.

After animation parsed, plays animation automatically.

#### loopCount: Int

Defaults to `0`.

How many times should animation loops. `0` means Infinity Loop.

#### clearsAfterStop: Boolean

Defaults to `true`.

Clears canvas after animation stop.

#### clearsAfterDetached: Boolean

Defaults to `true`.

Clears canvas after SVGAImageView detached.

#### fillMode: String

Defaults to `Forward`. Could be `Forward`, `Backward`.

`Forward` means animation will pause on last frame after finished.

`Backward` means animation will pause on first frame after finished.

### Using code

You may use code to add `SVGAImageView` either.

#### Create a `SVGAImageView` instance.

```kotlin
SVGAImageView imageView = new SVGAImageView(this);
```

#### Declare a static Parser instance.

```kotlin
parser = SVGAParser.shareParser()
```

#### Init parser instance 

You should initialize the parser instance with context before usage.
```
SVGAParser.shareParser().init(this);
```

Otherwise it will report an error:
`Log.e("SVGAParser", "在配置 SVGAParser context 前, 无法解析 SVGA 文件。")`

You can also create `SVGAParser` instance by yourself.

#### Create a `SVGAParser` instance, parse from assets like this.

```kotlin
parser = new SVGAParser(this);
parser.decodeFromAssets("posche.svga", new SVGAParser.ParseCompletion() {
    // ...
});
```

#### Create a `SVGAParser` instance, parse from remote server like this.

```kotlin
parser = new SVGAParser(this);
parser.decodeFromURL(new URL("https://github.com/yyued/SVGA-Samples/blob/master/posche.svga?raw=true"), new SVGAParser.ParseCompletion() {
    
});
```

#### Create a `SVGADrawable` instance then set to `SVGAImageView`, play it as you want.

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

### Cache

`SVGAParser` will not manage any cache, you need to setup cacher by yourself.

#### Setup HttpResponseCache

`SVGAParser` depends on `URLConnection`, `URLConnection` uses `HttpResponseCache` to cache things.

Add codes to `Application.java:onCreate` to setup cacher.

```kotlin
val cacheDir = File(context.applicationContext.cacheDir, "http")
HttpResponseCache.install(cacheDir, 1024 * 1024 * 128)
```

### SVGALogger
Updated the internal log output, which can be managed and controlled through SVGALogger. It is not activated by default. Developers can also implement the ILogger interface to capture and collect logs externally to facilitate troubleshooting
Set whether the log is enabled through the `setLogEnabled` method
Inject a custom ILogger implementation class through the `injectSVGALoggerImp` method

## Features

Here are many feature samples.

* [Replace an element with Bitmap.](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Image)
* [Add text above an element.](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Text)
* [Add static layout text above an element.](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Text-Layout)
* [Hides an element dynamicaly.](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Hidden)
* [Use a custom drawer for element.](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Drawer)

## APIs

Head on over to [https://github.com/yyued/SVGAPlayer-Android/wiki/APIs](https://github.com/yyued/SVGAPlayer-Android/wiki/APIs)

## CHANGELOG

Head on over to [CHANGELOG](./CHANGELOG.md)

## Credits

### Contributors

This project exists thanks to all the people who contribute. [[Contribute](CONTRIBUTING.md)].

<a href="https://github.com/yyued/SVGAPlayer-Android/graphs/contributors"><img src="https://opencollective.com/SVGAPlayer-Android/contributors.svg?width=890&button=false" /></a>

### Backers

Thank you to all our backers! 🙏 [[Become a backer](https://opencollective.com/SVGAPlayer-Android#backer)]

<a href="https://opencollective.com/SVGAPlayer-Android#backers" target="_blank"><img src="https://opencollective.com/SVGAPlayer-Android/backers.svg?width=890"></a>

### Sponsors

Support this project by becoming a sponsor. Your logo will show up here with a link to your website. [[Become a sponsor](https://opencollective.com/SVGAPlayer-Android#sponsor)]

<a href="https://opencollective.com/SVGAPlayer-Android/sponsor/0/website" target="_blank"><img src="https://opencollective.com/SVGAPlayer-Android/sponsor/0/avatar.svg"></a>

