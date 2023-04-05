# Async
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/anvell/async/check.yml?label=Build%20%26%20Test&style=flat-square)
[![](https://jitpack.io/v/anvell/async.svg?style=flat-square)](https://jitpack.io/#anvell/async)
[![License](https://img.shields.io/github/license/anvell/async.svg?style=flat-square)](https://github.com/anvell/async/blob/master/LICENSE)

![badge][badge-jvm]
![badge][badge-js]
![badge][badge-linux]
![badge][badge-windows]
![badge][badge-mac]
![badge][badge-ios]

Type describing generic asynchronous value.

Async value can be in either of the following states:

• `Uninitialized` - the value has not been initialized yet

• `Loading` - the value is being loaded 

• `Success` - stores successfully loaded `value` 

• `Fail` - failed to load the value with `error`

## Installation

Library is published on jitpack.io. Add repository it to your ```build.gradle``` script:
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
and:

```gradle
dependencies {
    implementation 'com.github.anvell:async:1.1.2'
}
```

## API Reference

In order to generate API Reference in `.html` format, run the following command:

```shell
$ ./gradlew dokkaHtmlMultiModule
```

## Licensing

Project is available under [MIT](https://github.com/Anvell/async/blob/master/LICENSE.txt) License.

[badge-jvm]: http://img.shields.io/badge/-JVM-DB413D.svg?style=flat-square
[badge-js]: http://img.shields.io/badge/-JS-F8DB5D.svg?style=flat-square
[badge-linux]: http://img.shields.io/badge/-Linux-2D3F6C.svg?style=flat-square
[badge-windows]: http://img.shields.io/badge/-Windows-4D76CD.svg?style=flat-square
[badge-ios]: http://img.shields.io/badge/-iOS-CDCDCD.svg?style=flat-square
[badge-mac]: http://img.shields.io/badge/-MacOs-808080.svg?style=flat-square
