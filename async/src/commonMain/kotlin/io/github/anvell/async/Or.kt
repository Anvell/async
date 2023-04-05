package io.github.anvell.async

inline fun <T> Async<T>.or(
    transform: (error: Throwable) -> Async<T>
): Async<T> = when (this) {
    is Uninitialized -> Uninitialized
    is Loading -> Loading(progress)
    is Success -> Success(value)
    is Fail -> transform(error)
}
