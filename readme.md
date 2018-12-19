# SVGAPlayer

[简体中文](./readme.zh.md)

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
compile 'com.github.yyued:SVGAPlayer-Android:2.3.0'
```

[![](https://jitpack.io/v/yyued/SVGAPlayer-Android.svg)](https://jitpack.io/#yyued/SVGAPlayer-Android)

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

#### Create a `SVGAParser` instance, parse from assets like this.

```kotlin
parser = new SVGAParser(this);
parser.parse("posche.svga", new SVGAParser.ParseCompletion() {
    
});
```

#### Create a `SVGAParser` instance, parse from remote server like this.

```kotlin
parser = new SVGAParser(this);
parser.parse(new URL("https://github.com/yyued/SVGA-Samples/blob/master/posche.svga?raw=true"), new SVGAParser.ParseCompletion() {
    
});
```

#### Create a `SVGADrawable` instance then set to `SVGAImageView`, play it as you want.

```kotlin
parser = new SVGAParser(this);
parser.parse(..., new SVGAParser.ParseCompletion() {
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

## Features

Here are many feature samples.

* [Replace an element with Bitmap.](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Image)
* [Add text above an element.](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Text)
* [Add static layout text above an element.](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Text-Layout)
* [Hides an element dynamicaly.](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Hidden)
* [Use a custom drawer for element.](https://github.com/yyued/SVGAPlayer-Android/wiki/Dynamic-Drawer)

## APIs

Head on over to [https://github.com/yyued/SVGAPlayer-Android/wiki/APIs](https://github.com/yyued/SVGAPlayer-Android/wiki/APIs)