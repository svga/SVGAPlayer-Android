# SVGAPlayer

## Version

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

尚未开源，请使用源码方式集成本库，或者下载源码后，在 library/ 目录下找到 aar 文件进行集成。

## 使用

### 初始化 Player

```
SVGAPlayer player = new SVGAPlayer(this);
```

### 初始化 Parser 并加载资源文件

在子线程中加载资源文件，在 UI 线程调用 Player 方法。

```
parser = new SVGAParser(this);
childThreadHandler().post(new Runnable() {
    @Override
    public void run() {
        try {
            String url = "http://legox.yy.com/svga/svga-me/angel.svga";
            final SVGAVideoEntity videoItem = parser.parse(new URL(url));
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    player.setVideoItem(videoItem);
                    player.startAnimation();

                }
            });
        } catch (Exception e) {
            System.out.println(e);
        }
    }
});
```

## API

### Properties
* int loops; - 循环次数，0 = 无限循环
* boolean clearsAfterStop; - 是否在结束播放时清空画布。

### Methods
* void setCallback(SVGAPlayerCallback callback) - 设置动画播放回调
* void setVideoItem(SVGAVideoEntity videoItem) - 设置当前动画实例
* boolean startAnimation() - 从 0 帧开始播放动画
* void pauseAnimation() - 在当前帧暂停动画
* void stopAnimation() - 停止播放动画，如果 clearsAfterStop == YES，则同时清空画布
* void stepToFrame(int frame, boolean andPlay) - 跳到第 N 帧 (frame 0 = 第 1 帧)，然后 andPlay == YES 时播放动画
* void stepToPercentage(float percentage, boolean andPlay) - 跳到动画对应百分比的帧，然后 andPlay == YES 时播放动画
* void setDynamicImage(BitmapDrawable drawable, String forKey) - 设置动态图像
* void setDynamicText(String text, TextPaint textPaint, String forKey) - 设置动态文本
* void clearDynamicObjects() - 清空动态图像和文本

### SVGAPlayerCallback

* void onPause(SVGAPlayer svgaPlayer) - 在暂停时调用
* void onFinished(SVGAPlayer svgaPlayer) - 在动画结束时调用
* void onStep(SVGAPlayer svgaPlayer, int frame, float percentage) - 在播放完某帧时调用

## 动态对象

SVGAPlayer 支持动态图像和动态文本，其添加方法如下。

### 动态图像

```
// 你需要自行生成一个 BitmapDrawable 对象
player.setDynamicImage(bitmapDrawable, "99");
```

### 动态文本

```
TextPaint textPaint = new TextPaint();
textPaint.setTextSize(30);
textPaint.setFakeBoldText(true);
textPaint.setARGB(0xff, 0xff, 0xe0, 0xa4);
textPaint.setShadowLayer((float)1.0, (float)0.0, (float)1.0, Color.BLACK); // 各种配置
player.setDynamicText("崔小姐不吃鱼 送了魔法奇缘", textPaint, "banner");
```