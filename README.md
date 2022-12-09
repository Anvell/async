# Async
![Build & Test](https://github.com/anvell/async/actions/workflows/check.yml/badge.svg) [![](https://jitpack.io/v/anvell/async.svg)](https://jitpack.io/#anvell/async)

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
    implementation 'com.github.anvell:async:0.2.1'
}
```

## API Reference

In order to generate API Reference in `.html` format, run the following command:

```shell
$ ./gradlew dokkaHtmlMultiModule
```

## Licensing

Project is available under [MIT](https://github.com/Anvell/async/blob/master/LICENSE.txt) License.
