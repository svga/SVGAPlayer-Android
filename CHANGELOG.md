# SVGAPlayer-Android CHANGELOG (2020-08-04)


## [2.5.8](https://github.com/svga/SVGAPlayer-Android/compare/2.5.7...2.5.8) (2020-08-04)

### Features

* Add SVGALogger. ([6ec28d1](https://github.com/svga/SVGAPlayer-Android/commit/6ec28d1))


## [2.5.7](https://github.com/svga/SVGAPlayer-Android/compare/2.5.6...2.5.7) (2020-07-21)

### Bug Fixes

* Fix the high failure rate of concurrent parsing of svga files. ([94a8616](https://github.com/svga/SVGAPlayer-Android/commit/94a8616))
* Correct the use of the default kotlin_module will easily cause some compilation conflicts. ([c786376](https://github.com/svga/SVGAPlayer-Android/commit/c786376))
* Correct zipper down path crossing problem. ([4a44db6](https://github.com/svga/SVGAPlayer-Android/commit/4a44db6))


## [2.5.6](https://github.com/svga/SVGAPlayer-Android/compare/2.5.5...2.5.6) (2020-07-17)

### Bug Fixes

* Correct app crashes on API level below 21 caused by constructor. ([06d8ad4](https://github.com/svga/SVGAPlayer-Android/commit/06d8ad4))


## [2.5.5](https://github.com/svga/SVGAPlayer-Android/compare/2.5.3...2.5.5) (2020-07-13)

### Bug Fixes

* Fix the problem that the audio file name is too short and the svga file analysis fails. ([71d2b87](https://github.com/svga/SVGAPlayer-Android/commit/71d2b87))
* Fix the memory leak problem of SVGAImageView and SVGAParser. ([7378862](https://github.com/svga/SVGAPlayer-Android/commit/7378862))
* Correct the memory release problem. ([dfc0edb](https://github.com/svga/SVGAPlayer-Android/commit/dfc0edb))


## [2.5.3](https://github.com/yyued/SVGAPlayer-Android/compare/2.5.2-beta...2.5.3) (2020-01-13)

### Bug Fixes

* Correct touch event. ([0133a0b](https://github.com/yyued/SVGAPlayer-Android/commit/0133a0b))
* Correct play audio. ([0133a0b](https://github.com/yyued/SVGAPlayer-Android/commit/0133a0b))
* Issue [#185](https://github.com/yyued/SVGAPlayer-Android/issues/185) drawTextOnBitmap 部分特殊文字会出现IndexOutOfBoundsException【必现】 ([1534cd2](https://github.com/yyued/SVGAPlayer-Android/commit/1534cd2))

### Features

* Add shareParser. ([3a4d5b9](https://github.com/yyued/SVGAPlayer-Android/commit/3a4d5b9))
* Clear audio when remove svga image view. ([35ec8ca](https://github.com/yyued/SVGAPlayer-Android/commit/35ec8ca))
* Correct touch event when mvideoItem == null; ([e50751e](https://github.com/yyued/SVGAPlayer-Android/commit/e50751e))
* Update threadPoolExecutor. ([3bc8915](https://github.com/yyued/SVGAPlayer-Android/commit/3bc8915))


## [2.5.2-beta](https://github.com/yyued/SVGAPlayer-Android/compare/2.4.4...2.5.2-beta) (2019-11-26)


### Bug Fixes

* Add canvas save and restore when Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP. ([f605c5b](https://github.com/yyued/SVGAPlayer-Android/commit/f605c5b))
* Add isRecycle check to avoid Bitmap recycled outside and draw. ([763a510](https://github.com/yyued/SVGAPlayer-Android/commit/763a510))
* Add try catch for resetImages to avoid OOM. ([ef1f232](https://github.com/yyued/SVGAPlayer-Android/commit/ef1f232))
* correct bitmap size. ([3ae8390](https://github.com/yyued/SVGAPlayer-Android/commit/3ae8390))
* Delete useless file. ([1a4ff57](https://github.com/yyued/SVGAPlayer-Android/commit/1a4ff57))
* Extra code causes antiAlias not effective. ([ac91e2a](https://github.com/yyued/SVGAPlayer-Android/commit/ac91e2a))
* filter no matte. ([1257db4](https://github.com/yyued/SVGAPlayer-Android/commit/1257db4))
* filter no matte. ([ae7e802](https://github.com/yyued/SVGAPlayer-Android/commit/ae7e802))
* Ignore matte layer when Build.VERSION.SDK_INT unsupport. ([0db05f2](https://github.com/yyued/SVGAPlayer-Android/commit/0db05f2))
* Remove clipPath support. ([f9e3827](https://github.com/yyued/SVGAPlayer-Android/commit/f9e3827))
* reset image when bitmap matte layer. ([3f06512](https://github.com/yyued/SVGAPlayer-Android/commit/3f06512))
* Restore audio prepare block. ([193c7d9](https://github.com/yyued/SVGAPlayer-Android/commit/193c7d9))
* return share clear bitmap when matte bitmap is null for avoiding crash. ([9e1f0f3](https://github.com/yyued/SVGAPlayer-Android/commit/9e1f0f3))
* support reuse bitmap paint and canvas. ([3df95bb](https://github.com/yyued/SVGAPlayer-Android/commit/3df95bb))
* update filter when matte sprite frame alpha = 0, it is visuable. ([b25fafb](https://github.com/yyued/SVGAPlayer-Android/commit/b25fafb))
* Use shared ThreadPoolExecutor avoid p_thread create OOM. ([e6d72ef](https://github.com/yyued/SVGAPlayer-Android/commit/e6d72ef))


### Features

* add 2.x proto support for matte. ([741eb01](https://github.com/yyued/SVGAPlayer-Android/commit/741eb01))
* Add dynamicDrawerSized logic. ([f37722f](https://github.com/yyued/SVGAPlayer-Android/commit/f37722f))
* Add StaticLayout maxLines support. ([fd63642](https://github.com/yyued/SVGAPlayer-Android/commit/fd63642))
* Avoid null sprite. ([25eb9ea](https://github.com/yyued/SVGAPlayer-Android/commit/25eb9ea))
* Catch Error OOM. ([8070ec6](https://github.com/yyued/SVGAPlayer-Android/commit/8070ec6))
* draw matte sprite with PorterDuffXfermode(PorterDuff.Mode.DST_IN). ([3a39ff6](https://github.com/yyued/SVGAPlayer-Android/commit/3a39ff6))
* draw matte sprite with PorterDuffXfermode(PorterDuff.Mode.DST_IN). ([bd55948](https://github.com/yyued/SVGAPlayer-Android/commit/bd55948))
* Set ParseCompletion Nullable. ([41b2c8f](https://github.com/yyued/SVGAPlayer-Android/commit/41b2c8f))
* Set parser class variables for demo. ([ae36dc3](https://github.com/yyued/SVGAPlayer-Android/commit/ae36dc3))
* Update matte draw logic. ([07e7d11](https://github.com/yyued/SVGAPlayer-Android/commit/07e7d11))

## [2.5.0](https://github.com/yyued/SVGAPlayer-Android/tree/2.5.0-release)(2019-10-16)

### Bug Fixes

* Add try catch for resetImages to avoid OOM. ([ef1f232](https://github.com/yyued/SVGAPlayer-Android/commit/ef1f232))
* Correct bitmap size. ([3ae8390](https://github.com/yyued/SVGAPlayer-Android/commit/3ae8390))
* Extra code causes antiAlias not effective. ([ac91e2a](https://github.com/yyued/SVGAPlayer-Android/commit/ac91e2a))
* Filter no matte. ([ae7e802](https://github.com/yyued/SVGAPlayer-Android/commit/ae7e802))
* Remove clipPath support. ([f9e3827](https://github.com/yyued/SVGAPlayer-Android/commit/f9e3827))
* reset image when bitmap matte layer. ([3f06512](https://github.com/yyued/SVGAPlayer-Android/commit/3f06512))
* Restore audio prepare block. ([193c7d9](https://github.com/yyued/SVGAPlayer-Android/commit/193c7d9))
* Return share clear bitmap when matte bitmap is null for avoiding crash. ([9e1f0f3](https://github.com/yyued/SVGAPlayer-Android/commit/9e1f0f3))
* Support reuse bitmap paint and canvas. ([3df95bb](https://github.com/yyued/SVGAPlayer-Android/commit/3df95bb))
* Update filter when matte sprite frame alpha = 0, it is visuable. ([b25fafb](https://github.com/yyued/SVGAPlayer-Android/commit/b25fafb))
* Use shared ThreadPoolExecutor avoid p_thread create OOM. ([e6d72ef](https://github.com/yyued/SVGAPlayer-Android/commit/e6d72ef))


### Features

* Add 2.x proto support for matte. ([741eb01](https://github.com/yyued/SVGAPlayer-Android/commit/741eb01))
* Add dynamicDrawerSized logic. ([f37722f](https://github.com/yyued/SVGAPlayer-Android/commit/f37722f))
* Catch Error OOM. ([8070ec6](https://github.com/yyued/SVGAPlayer-Android/commit/8070ec6))
* Set ParseCompletion Nullable. ([41b2c8f](https://github.com/yyued/SVGAPlayer-Android/commit/41b2c8f))
* Set parser class variables for demo. ([ae36dc3](https://github.com/yyued/SVGAPlayer-Android/commit/ae36dc3))
* Update matte draw logic. ([07e7d11](https://github.com/yyued/SVGAPlayer-Android/commit/07e7d11))

## [2.4.4](https://github.com/yyued/SVGAPlayer-Android/compare/2.4.3...2.4.4) (2019-05-15)


### Bug Fixes

* Add finalize method to release some resources. ([8506240](https://github.com/yyued/SVGAPlayer-Android/commit/8506240))
* Add protected keyword to finalize. ([197f4f9](https://github.com/yyued/SVGAPlayer-Android/commit/197f4f9))
* Remove recycle operation on finalize method, this line due to crash on some devices. ([a0c5a79](https://github.com/yyued/SVGAPlayer-Android/commit/a0c5a79))



## [2.4.2](https://github.com/yyued/SVGAPlayer-Android/compare/2.4.0...2.4.2) (2019-01-21)


### Bug Fixes

* https://github.com/yyued/SVGAPlayer-Android/issues/110 ([38fba4f](https://github.com/yyued/SVGAPlayer-Android/commit/38fba4f))



## [2.4.0](https://github.com/yyued/SVGAPlayer-Android/compare/2.3.0...2.4.0) (2019-01-16)


### Bug Fixes

* Fix fail to play 1.0 format file. ([7fad1cd](https://github.com/yyued/SVGAPlayer-Android/commit/7fad1cd))
* Fix memory issue, due to android.view.ImageView drawable cycle reference, let drawable sets to WeakReference if ImageView detached. ([d040e36](https://github.com/yyued/SVGAPlayer-Android/commit/d040e36))
* Remove unnecessary code. ([cd31b1b](https://github.com/yyued/SVGAPlayer-Android/commit/cd31b1b))
* Fix stroke color did not apply sprite alpha. ([2077be9](https://github.com/yyued/SVGAPlayer-Android/commit/2077be9))


## 2.3.0 

*  Add audio support. 

## 2.1.10 

* Fix vector stroke width scale for old version.

## 2.1.9

* Fix alpha not set while drawing shapes.

## 2.1.8 

* Handle null return for func readAsBytes



