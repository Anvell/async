package io.github.anvell.async

import java.util.concurrent.atomic.AtomicBoolean

sealed class Async<out T> {
    open operator fun invoke(): T? = null

    fun unwrap(): T = requireNotNull(invoke())
}

object Uninitialized : Async<Nothing>()

object Loading : Async<Nothing>()

data class Success<out T>(
    val value: T,
    val metadata: Any? = null
) : Async<T>() {
    private val consumed = AtomicBoolean(false)
    val isConsumed
        get() = consumed.get()

    override operator fun invoke(): T {
        consumed.set(true)
        return value
    }

    fun peek() = value
}

data class Fail<out T>(
    private val throwable: Throwable,
    val metadata: Any? = null
) : Async<T>() {
    private val consumed = AtomicBoolean(false)
    val isConsumed
        get() = consumed.get()

    val error: Throwable
        get() {
            consumed.set(true)
            return throwable
        }

    fun peekError() = throwable
}
