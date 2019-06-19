# SVGAPlayer-Android (2019-06-19)

## [2.5.0](https://github.com/yyued/SVGAPlayer-Android/compare/2.5.0...2.4.4) (2019-06-19)

### Bug Fixes

* correct bitmap size. ([3ae8390](https://github.com/yyued/SVGAPlayer-Android/commit/3ae8390))
* correct demo. ([c4d71e8](https://github.com/yyued/SVGAPlayer-Android/commit/c4d71e8))
* correct image key. ([b8a85db](https://github.com/yyued/SVGAPlayer-Android/commit/b8a85db))
* filter no matte. ([1257db4](https://github.com/yyued/SVGAPlayer-Android/commit/1257db4))
* filter no matte. ([ae7e802](https://github.com/yyued/SVGAPlayer-Android/commit/ae7e802))
* Remove clipPath support. ([f9e3827](https://github.com/yyued/SVGAPlayer-Android/commit/f9e3827))
* reset image when bitmap matte layer. ([3f06512](https://github.com/yyued/SVGAPlayer-Android/commit/3f06512))
* return share clear bitmap when matte bitmap is null for avoiding crash. ([9e1f0f3](https://github.com/yyued/SVGAPlayer-Android/commit/9e1f0f3))
* support reuse bitmap paint and canvas. ([3df95bb](https://github.com/yyued/SVGAPlayer-Android/commit/3df95bb))
* update filter when matte sprite frame alpha = 0, it is visuable. ([b25fafb](https://github.com/yyued/SVGAPlayer-Android/commit/b25fafb))


### Features

* add 2.x proto support for matte. ([741eb01](https://github.com/yyued/SVGAPlayer-Android/commit/741eb01))
* add 2.x proto support for matte. ([895edf0](https://github.com/yyued/SVGAPlayer-Android/commit/895edf0))
* draw matte sprite with PorterDuffXfermode(PorterDuff.Mode.DST_IN). ([3a39ff6](https://github.com/yyued/SVGAPlayer-Android/commit/3a39ff6))
* draw matte sprite with PorterDuffXfermode(PorterDuff.Mode.DST_IN). ([bd55948](https://github.com/yyued/SVGAPlayer-Android/commit/bd55948))


### Reverts

* #f9e3827. ([0a95581](https://github.com/yyued/SVGAPlayer-Android/commit/0a95581)), closes [#f9e3827](https://github.com/yyued/SVGAPlayer-Android/issues/f9e3827)



## [2.4.4](https://github.com/yyued/SVGAPlayer-Android/compare/2.4.3...2.4.4) (2019-05-15)


### Bug Fixes

* Add finalize method to release some resources. ([8506240](https://github.com/yyued/SVGAPlayer-Android/commit/8506240))
* Add protected keyword to finalize. ([197f4f9](https://github.com/yyued/SVGAPlayer-Android/commit/197f4f9))
* Remove recycle operation on finalize method, this line due to crash on some devices. ([a0c5a79](https://github.com/yyued/SVGAPlayer-Android/commit/a0c5a79))



## [2.4.2](https://github.com/yyued/SVGAPlayer-Android/compare/2.4.0...2.4.2) (2019-01-21)


### Bug Fixes

* https://github.com/yyued/SVGAPlayer-Android/issues/110 ([38fba4f](https://github.com/yyued/SVGAPlayer-Android/commit/38fba4f))



# [2.4.0](https://github.com/yyued/SVGAPlayer-Android/compare/2.3.0...2.4.0) (2019-01-16)


### Bug Fixes

* fail to play 1.0 format file. ([7fad1cd](https://github.com/yyued/SVGAPlayer-Android/commit/7fad1cd))
* Memory issue, due to android.view.ImageView drawable cycle reference, let drawable sets to WeakReference if ImageView detached. ([d040e36](https://github.com/yyued/SVGAPlayer-Android/commit/d040e36))
* Remove unnecessary code. ([cd31b1b](https://github.com/yyued/SVGAPlayer-Android/commit/cd31b1b))
* Stroke color did not apply sprite alpha. ([2077be9](https://github.com/yyued/SVGAPlayer-Android/commit/2077be9))



# [2.3.0](https://github.com/yyued/SVGAPlayer-Android/compare/2.1.9...2.3.0) (2018-10-31)



## [2.1.9](https://github.com/yyued/SVGAPlayer-Android/compare/2.1.8...2.1.9) (2018-09-18)



## [2.1.7](https://github.com/yyued/SVGAPlayer-Android/compare/2.1.5...2.1.7) (2018-08-10)



## [2.1.5](https://github.com/yyued/SVGAPlayer-Android/compare/2.1.4...2.1.5) (2018-07-11)



## [2.1.4](https://github.com/yyued/SVGAPlayer-Android/compare/2.1.3...2.1.4) (2018-04-09)



## [2.1.2](https://github.com/yyued/SVGAPlayer-Android/compare/2.1.1...2.1.2) (2018-03-28)



## [2.1.1](https://github.com/yyued/SVGAPlayer-Android/compare/2.1.0...2.1.1) (2018-01-31)



# [2.1.0](https://github.com/yyued/SVGAPlayer-Android/compare/2.0.6...2.1.0) (2018-01-15)



## [2.0.6](https://github.com/yyued/SVGAPlayer-Android/compare/2.0.5...2.0.6) (2017-12-12)



## [2.0.5](https://github.com/yyued/SVGAPlayer-Android/compare/2.0.4...2.0.5) (2017-11-24)



## [2.0.4](https://github.com/yyued/SVGAPlayer-Android/compare/2.0.3...2.0.4) (2017-11-24)



## [2.0.3](https://github.com/yyued/SVGAPlayer-Android/compare/2.0.2...2.0.3) (2017-11-17)



## [2.0.2](https://github.com/yyued/SVGAPlayer-Android/compare/2.0.1...2.0.2) (2017-11-09)



## [2.0.1](https://github.com/yyued/SVGAPlayer-Android/compare/2.0.0...2.0.1) (2017-11-07)



# [2.0.0](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.10...2.0.0) (2017-10-23)



## [1.2.10](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.9...1.2.10) (2017-10-17)



## [1.2.9](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.8...1.2.9) (2017-10-16)



## [1.2.8](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.7...1.2.8) (2017-08-21)



## [1.2.7](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.6...1.2.7) (2017-08-07)



## [1.2.6](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.5...1.2.6) (2017-08-04)



## [1.2.5](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.4...1.2.5) (2017-08-01)



## [1.2.4](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.3...1.2.4) (2017-06-19)



## [1.2.3](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.2...1.2.3) (2017-06-15)



## [1.2.2](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.1...1.2.2) (2017-05-15)



## [1.2.1](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.0-beta5...1.2.1) (2017-05-12)



# [1.2.0-beta5](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.0...1.2.0-beta5) (2017-04-28)



# [1.2.0-beta4](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.0-beta3...1.2.0-beta4) (2017-04-20)



# [1.2.0-beta3](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.0-beta2...1.2.0-beta3) (2017-04-01)



# [1.2.0-beta2](https://github.com/yyued/SVGAPlayer-Android/compare/1.2.0-beta...1.2.0-beta2) (2017-03-31)



# [1.2.0-beta](https://github.com/yyued/SVGAPlayer-Android/compare/1.1.0...1.2.0-beta) (2017-03-30)



# [1.1.0](https://github.com/yyued/SVGAPlayer-Android/compare/1.1.0-beta...1.1.0) (2017-03-23)



# 1.1.0-beta (2017-02-22)



