# SVGAPlayer

[简体中文](./readme.zh.md)

## 支持本项目

1. 轻点 GitHub Star，让更多人看到该项目。

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

#### ~~clearsAfterStop: Boolean~~

Defaults to `false`.When the animation is finished, whether to clear the canvas and the internal data of SVGAVideoEntity.
It is no longer recommended. Developers can control resource release through clearAfterDetached, or manually control resource release through SVGAVideoEntity#clear

#### clearsAfterDetached: Boolean

Defaults to `false`.Clears canvas and the internal data of SVGAVideoEntity after SVGAImageView detached.

#### fillMode: String

Defaults to `Forward`. Could be `Forward`, `Backward`, `Clear`.

`Forward` means animation will pause on last frame after finished.

`Backward` means animation will pause on first frame after finished.

`Clear` after the animation is played, all the canvas content is cleared, but it is only the canvas and does not involve the internal data of SVGAVideoEntity.

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
// The third parameter is a default parameter, which is null by default. If this method is set, the audio parsing and playback will not be processed internally. The audio File instance will be sent back to the developer through PlayCallback, and the developer will control the audio playback and playback. stop
parser.decodeFromAssets("posche.svga", object : SVGAParser.ParseCompletion {
    // ...
}, object : SVGAParser.PlayCallback {
    // The default is null, can not be set
})
```

#### Create a `SVGAParser` instance, parse from remote server like this.

```kotlin
parser = new SVGAParser(this);
// The third parameter is a default parameter, which is null by default. If this method is set, the audio parsing and playback will not be processed internally. The audio File instance will be sent back to the developer through PlayCallback, and the developer will control the audio playback and playback. stop
parser.decodeFromURL(new URL("https://github.com/yyued/SVGA-Samples/blob/master/posche.svga?raw=true"), new SVGAParser.ParseCompletion() {
    // ...
}, object : SVGAParser.PlayCallback {
    // The default is null, can not be set
})
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


```kotlin

// By default, SVGA will not output any log, so you need to manually set it to true
SVGALogger.setLogEnabled(true)

// If you want to collect the output log of SVGA, you can obtain it in the following way
SVGALogger.injectSVGALoggerImp(object: ILogger {
// Implement related interfaces to receive log
})
```

### SVGASoundManager
Added SVGASoundManager to control SVGA audio, you need to manually call the init method to initialize, otherwise follow the default audio loading logic.
In addition, through SVGASoundManager#setVolume, you can control the volume of SVGA playback. The range is [0f, 1f]. By default, the volume of all SVGA playbacks is controlled.
And this method can set a second default parameter: SVGAVideoEntity, which means that only the current SVGA volume is controlled, and the volume of other SVGAs remains unchanged.

```kotlin
// Initialize the audio manager for easy management of audio playback
// If it is not initialized, the audio will be loaded in the original way by default
SVGASoundManager.init()

// Release audio resources
SVGASoundManager.release()

/**
* Set the volume level, entity is null by default
* When entity is null, it controls the volume of all audio loaded through SVGASoundManager, which includes the currently playing audio and subsequent loaded audio
* When entity is not null, only the SVGA audio volume of the instance is controlled, and the others are not affected
* 
* @param volume The value range is [0f, 1f]
* @param entity That is, the instance of SVGAParser callback
*/
SVGASoundManager.setVolume(volume, entity)
```

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

