# SVGAPlayer

## SVGA Format

* SVGA 是一个私有的动画格式，由 YY UED 主导开发。
* SVGA 由 SVG 演进而成，与 SVG 不兼容。
* SVGA 可以在 iOS / Android / Web(PC/移动端) 实现高性能的动画播放。

## 性能

600 * 600 像素的全通道动画，144 Frames， FPS = 30：

### 解码 (华为 Honor P7)
* 峰值：CPU 小于 25% Memory 小于 35.0M
* 持续时间：小于 200ms

### 播放 (华为 Honor P7)
* 峰值：CPU 小于 10% Memory 小于 35.5M
* 持续时间：一直

### 解码 (小米 2S)
* 峰值：CPU 小于 35% Memory 小于 35.5M
* 持续时间：小于 150ms

### 播放 (小米 2S)
* 峰值：CPU 小于 20% Memory 小于 20.5M
* 持续时间：一直

## SVGA 数据流大小

一个 SVGA 文件包括所有的素材以及序列数据，一个 600 * 600 像素的全通道动画，包含 144 Frames，未压缩前大小为 800K，压缩后大小为 280K。

** 压缩工具为 gzip pngquant ** 

## SVGA 的优势

### 对比 WebP / A-PNG

WebP 以及 A-PNG 均支持全通道动画格式，在小动画播放上优势明显，但缺点在于其播放大动画时内存或CPU(GPU)占用非常严重，究其原因，终究未能逃脱逐帖逐像素点渲染的陷阱。

### 对比 MP4

MP4 不支持透明通道，不应该考虑作礼物类动画。

### 对比 GIF

GIF 与 WebP / A-PNG 原理，实际上是一致的，并且其不支持半透明通道，在播放过程中，会有白边产生，颜色也只支持 256 色。

### SVGA

SVGA 在文件大小上，远小于以上格式，并且 SVGA 是一种无损压缩格式，不会影响动画效果。 SVGA 在渲染、播放过程中，只会操纵可变元素的位移、透明度、缩放等参数，开销远小于逐帖渲染方案。

## 原理

一个 SVGA 动画由多个元素构成，比较一个天使，可以拆分为头、手、脚、身四个部分（具体如何切分，是由设计师决定的），这些元素是可以活动的，不同的活动参数构成完整的一帖。

在动画播放的过程中，SVGA 不需要重新渲染新的一帖，SVGA 采取的方法是，找到变化的元素，改变它的参数，使其产生位移、透明度变化等特征。

一个动画可以理解为 元素 + 参数 = 帖， 帖 + 帖 = 动画。

同时，在 Android 上，使用 Canvas 进行渲染。

## 工作方式

* SVGA 对于设计师来说十分友好， 它并没有要求设计师使用私有的设计工具。 
* 设计师可以使用任意位图工具（例如 Photoshop）进行素描，然后使用（Flash Professional / Animate CC）进行动画的构建。
* 最后，设计师可以通过插件，自行导出 SVG 序列帖，再通过 SVGAConverter 进行格式转换即可。

## 客户端调用方法

直接将 library 文件夹复制到目标应用进行集成

一个 SVGA 文件可以由 SVGAPlayer 进行播放。

在 Android 中，你需要自行处理 SVGA 文件的下载，然后使用以下方法播放视频。

```
SVGAPlayer player = new SVGAPlayer(this);
player.setVideoWidth((int)(getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().scaledDensity));
player.loops = 0;
player.clearsAfterStop = true;
try {
    InputStream inputStream = this.getAssets().open("test.svga");
    SVGAParser parser = new SVGAParser();
    try {
        player.setVideoItem(parser.parse(inputStream));
        player.startAnimation();
    } catch (Exception e) {
        e.printStackTrace();
    }
} catch (IOException e) {
    e.printStackTrace();
}
```

## 动态对象

自 0.1.0 起，SVGAPlayer 支持动态图像和动态文本，其添加方法如下。

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

## 参数设置

* FPS, FPS 由 SVGA 动画自身决定，客户端不能修改，在 SVGAConverter 转换的过程中添加该参数，默认的 FPS = 20。
* loops, 循环次数，如果设为 0，则会一直播放，设为 1，则在播放一次后停止。
* clearsAfterStop, 是否在停止播放后清空画布，默认为 false。 

## 开源协议

* 本项目尚未开源，属于私有项目，请勿向外传播。