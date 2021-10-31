package io.github.anvell.async

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Describes generic asynchronous `value`.
 *
 * @param T The type of the `value`.
 */
sealed class Async<out T> {
    /**
     * Returns the `value` or `null`.
     */
    open operator fun invoke(): T? = null

    /**
     * Returns the `value` if present, throws an [IllegalArgumentException] otherwise.
     */
    fun unwrap(): T = requireNotNull(invoke())
}

object Uninitialized : Async<Nothing>()

object Loading : Async<Nothing>()

/**
 * Stores successfully loaded [value].
 */
data class Success<out T>(val value: T) : Async<T>() {
    private val consumed = AtomicBoolean(false)

    /**
     * Describes whether the [value] has been consumed.
     */
    val isConsumed
        get() = consumed.get()

    /**
     * Returns stored [value].
     */
    override operator fun invoke(): T {
        consumed.set(true)
        return value
    }

    /**
     * Peeks the [value] without consuming it.
     */
    fun peek() = value
}

/**
 * Represents failure when loading the `value` and stores corresponding [error].
 */
data class Fail<out T>(private val throwable: Throwable) : Async<T>() {
    private val consumed = AtomicBoolean(false)

    /**
     * Describes whether the [error] has been consumed.
     */
    val isConsumed
        get() = consumed.get()

    /**
     * Consumes and returns stored `error`.
     */
    val error: Throwable
        get() {
            consumed.set(true)
            return throwable
        }

    /**
     * Peeks the [error] without consuming it.
     */
    fun peekError() = throwable
}
