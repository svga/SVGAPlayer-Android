# SVGAPlayer-Android CHANGELOG (2019-10-16)

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

## [2.4.4](https://github.com/yyued/SVGAPlayer-Android/compare/2.4.3...2.4.4) (2019-05-15)


### Bug Fixes

* Add finalize method to release some resources. ([8506240](https://github.com/yyued/SVGAPlayer-Android/commit/8506240))
* Add protected keyword to finalize. ([197f4f9](https://github.com/yyued/SVGAPlayer-Android/commit/197f4f9))
* Remove recycle operation on finalize method, this line due to crash on some devices. ([a0c5a79](https://github.com/yyued/SVGAPlayer-Android/commit/a0c5a79))



## [2.4.2](https://github.com/yyued/SVGAPlayer-Android/compare/2.4.0...2.4.2) (2019-01-21)


### Bug Fixes

* https://github.com/yyued/SVGAPlayer-Android/issues/110 ([38fba4f](https://github.com/yyued/SVGAPlayer-Android/commit/38fba4f))



## [2.4.0](https://github.com/yyued/SVGAPlayer-Android/compare/2.3.0...2.4.0) (2019-01-16)


### Bug Fixes

* Fix fail to play 1.0 format file. ([7fad1cd](https://github.com/yyued/SVGAPlayer-Android/commit/7fad1cd))
* Fix memory issue, due to android.view.ImageView drawable cycle reference, let drawable sets to WeakReference if ImageView detached. ([d040e36](https://github.com/yyued/SVGAPlayer-Android/commit/d040e36))
* Remove unnecessary code. ([cd31b1b](https://github.com/yyued/SVGAPlayer-Android/commit/cd31b1b))
* Fix stroke color did not apply sprite alpha. ([2077be9](https://github.com/yyued/SVGAPlayer-Android/commit/2077be9))
 
## 2.3.0 

*  Add audio support. 

## 2.1.10 

* Fix vector stroke width scale for old version.

## 2.1.9

* Fix alpha not set while drawing shapes.

## 2.1.8 

* Handle null return for func readAsBytes



