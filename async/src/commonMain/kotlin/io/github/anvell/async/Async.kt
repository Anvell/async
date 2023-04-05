package io.github.anvell.async

/**
 * Describes generic asynchronous [value].
 *
 * @param T The type of the [value].
 */
public sealed class Async<out T> {

    /**
     * By default, [value] is set to `null`.
     */
    public open val value: T? get() = null

    /**
     * Unwraps the [value] if present, throws an [IllegalArgumentException] otherwise.
     *
     * @param message optional custom error message.
     * @return not optional [value]
     */
    public fun unwrap(message: String? = null): T = if (message != null) {
        requireNotNull(value) { message }
    } else {
        requireNotNull(value)
    }
}

/**
 * Represents uninitialized state.
 */
public object Uninitialized : Async<Nothing>()

/**
 * Represents loading state with optional progress rate.
 */
public data class Loading(
    /*@FloatRange(from = 0.0, to = 1.0)*/
    val progress: Float? = null
) : Async<Nothing>()

/**
 * Stores successfully loaded [value].
 */
public data class Success<out T>(override val value: T) : Async<T>()

/**
 * Represents failure when loading the [value] and stores corresponding [error].
 */
public data class Fail<out T>(val error: Throwable) : Async<T>()
