# SVGAPlayer-Android (2019-06-20)

## [Next release](https://github.com/yyued/SVGAPlayer-Android/compare/2.5.0...2.4.4) (2019-06-20)

### Bug Fixes

* Support reuse bitmap paint and canvas. ([3df95bb](https://github.com/yyued/SVGAPlayer-Android/commit/3df95bb))
* Update filter when matte sprite frame alpha = 0, it is visuable. ([b25fafb](https://github.com/yyued/SVGAPlayer-Android/commit/b25fafb))


### Features

* Add 2.x proto support for matte. ([741eb01](https://github.com/yyued/SVGAPlayer-Android/commit/741eb01))
* Draw matte sprite with PorterDuffXfermode(PorterDuff.Mode.DST_IN). ([3a39ff6](https://github.com/yyued/SVGAPlayer-Android/commit/3a39ff6))

## [2.4.4](https://github.com/yyued/SVGAPlayer-Android/compare/2.4.3...2.4.4) (2019-05-15)


### Bug Fixes

* Add finalize method to release some resources. ([8506240](https://github.com/yyued/SVGAPlayer-Android/commit/8506240))
* Add protected keyword to finalize. ([197f4f9](https://github.com/yyued/SVGAPlayer-Android/commit/197f4f9))
* Remove recycle operation on finalize method, this line due to crash on some devices. ([a0c5a79](https://github.com/yyued/SVGAPlayer-Android/commit/a0c5a79))



## [2.4.2](https://github.com/yyued/SVGAPlayer-Android/compare/2.4.0...2.4.2) (2019-01-21)


### Bug Fixes

* https://github.com/yyued/SVGAPlayer-Android/issues/110 ([38fba4f](https://github.com/yyued/SVGAPlayer-Android/commit/38fba4f))



## [2.4.0](https://github.com/yyued/SVGAPlayer-Android/compare/2.3.0...2.4.0) (2019-01-16)


### Bug Fixes

* Fix fail to play 1.0 format file. ([7fad1cd](https://github.com/yyued/SVGAPlayer-Android/commit/7fad1cd))
* Fix memory issue, due to android.view.ImageView drawable cycle reference, let drawable sets to WeakReference if ImageView detached. ([d040e36](https://github.com/yyued/SVGAPlayer-Android/commit/d040e36))
* Remove unnecessary code. ([cd31b1b](https://github.com/yyued/SVGAPlayer-Android/commit/cd31b1b))
* Fix stroke color did not apply sprite alpha. ([2077be9](https://github.com/yyued/SVGAPlayer-Android/commit/2077be9))
 
## 2.3.0 

*  Add audio support. 

## 2.1.10 

* Fix vector stroke width scale for old version.

## 2.1.9

* Fix alpha not set while drawing shapes.

## 2.1.8 

* Handle null return for func readAsBytes


