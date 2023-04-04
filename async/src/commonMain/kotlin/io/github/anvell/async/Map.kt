package io.github.anvell.async

inline fun <R, T> Async<T>.map(
    transform: (value: T) -> R,
): Async<R> = when (this) {
    is Uninitialized -> Uninitialized
    is Loading -> Loading(progress)
    is Success -> Success(transform(value))
    is Fail -> Fail(error)
}
