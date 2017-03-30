# SVGAPlayer

## Version

### 1.2.0-beta

SVGAPlayer 的第 3 个版本，使用 Kotlin 重写整个 SVGAPlayer。

### 1.1.0-beta

SVGAPlayer 的第 2 个版本，对应 SVGA-1.1.0 协议，支持矢量动画，向下兼容 SVGA-1.0.0 协议。

### 0.1.0

SVGAPlayer 的第 1 个版本，对应 SVGA-1.0.0 协议，支持位图（位移、旋转、拉伸、透明度）动画。

## SVGA Format

* SVGA 是一个私有的动画格式，由 YY UED 主导开发。
* SVGA 由 SVG 演进而成，与 SVG 不兼容。
* SVGA 可以在 iOS / Android / Web(PC/移动端) 实现高性能的动画播放。

@see http://code.yy.com/ued/SVGA-Format

## 安装

### Gradle 

## 使用

### Layout.xml

你可以使用 layout.xml 添加 SVGAImageView 用以播放动画。

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

* source - SVGA 动画文件的路径，相对 assets 目录。
* autoPlay - 默认值为 true，为 true 时，动画加载完毕后，开始播放动画。
* loopCount - 默认值为 0，用于指定动画循环次数，0 = 无限循环。
* clearsAfterStop - 默认值为 true，动画播放完成后，是否清空画布。
* fillMode - 默认值为 Forward，可选值为 Forward / Backward，fillMode = Forward时，动画播放结束后会停留在最后一帧，fillMode = Backward 时，动画播放结束后会停留在第一帧。

### 代码方式

也可以使用纯代码方式加载动画文件，并添加 SVGAImageView 至界面。

#### 初始化 ImageView

```
SVGAImageView imageView = new SVGAImageView(this);
```

### 初始化 Parser 并加载资源文件

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

## API

### Properties Setter

* setLoops(int loops); - 循环次数，0 = 无限循环
* setClearsAfterStop(boolean clearsAfterStop); - 是否在结束播放时清空画布。
* setFillMode(FillMode fillMode); - 可选值为 Forward / Backward，fillMode = Forward时，动画播放结束后会停留在最后一帧，fillMode = Backward 时，动画播放结束后会停留在第一帧。
* setCallback(SVGAPlayerCallback callback) - 设置动画播放回调
* setVideoItem(SVGAVideoEntity videoItem) - 设置当前动画实例

### Methods
* startAnimation() - 从 0 帧开始播放动画
* pauseAnimation() - 在当前帧暂停动画
* stopAnimation() - 停止播放动画，如果 clearsAfterStop == YES，则同时清空画布
* stepToFrame(int frame, boolean andPlay) - 跳到第 N 帧 (frame 0 = 第 1 帧)，然后 andPlay == YES 时播放动画
* stepToPercentage(float percentage, boolean andPlay) - 跳到动画对应百分比的帧，然后 andPlay == YES 时播放动画

### SVGAPlayerCallback

* void onPause() - 在暂停时调用
* void onFinished() - 在动画结束时调用
* void onRepeat() - 在动画重复播放开始时调用
* void onStep(int frame, float percentage) - 在播放完某帧时调用

## 动态对象

SVGAPlayer 支持动态图像和动态文本，要添加动态图像和动态文本，你需要创建一个 SVGADynamicEntity 对象，并传入 SVGDrawable 初始化方法。

```
SVGADynamicEntity dynamicItem = new SVGADynamicEntity();
SVGADrawable drawable = new SVGADrawable(videoItem, dynamicItem);
```

### 添加动态图像

你需要自行生成一个 Bitmap 对象，然后执行 SVGADynamicEntity 中的方法，其中 forKey 是由设计师提供的。

```
dynamicItem.setDynamicImage(bitmapDrawable, "99");
```

### 添加动态文本

执行 SVGADynamicEntity 中的方法，其中 forKey 是由设计师提供的。

```
TextPaint textPaint = new TextPaint();
textPaint.setTextSize(30);
textPaint.setFakeBoldText(true);
textPaint.setARGB(0xff, 0xff, 0xe0, 0xa4);
textPaint.setShadowLayer((float)1.0, (float)0.0, (float)1.0, Color.BLACK); // 各种配置
dynamicItem.setDynamicText("崔小姐不吃鱼 送了魔法奇缘", textPaint, "banner");
```