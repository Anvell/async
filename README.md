# Async

---

Type describing generic asynchronous value.

Async value can be in either of the following states:

• `Uninitialized` - value has not been initialized yet

• `Loading` - value is being loaded 

• `Success` - stores successfully loaded `value` 

• `Fail` - failed to load value with `error`

## Installation

---

Gradle is the only supported build configuration.

`Async` has not yet been published to any public repository.

Suggested installation methods:

• Via [JitPack](https://jitpack.io/) plugin

• Setup and distribute via [Local Maven repository](https://docs.gradle.org/current/userguide/declaring_repositories.html)

## Licensing

---

Project is available under [MIT](https://github.com/Anvell/async/blob/master/LICENSE.txt) License.
