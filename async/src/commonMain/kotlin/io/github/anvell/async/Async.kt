package io.github.anvell.async

/**
 * Describes generic asynchronous [value].
 *
 * @param T The type of the [value].
 */
sealed class Async<out T> {

    /**
     * By default, [value] is set to `null`.
     */
    open val value: T? get() = null

    /**
     * Unwraps the [value] if present, throws an [IllegalArgumentException] otherwise.
     *
     * @param message optional custom error message.
     * @return not optional [value]
     */
    fun unwrap(message: String? = null): T = if (message != null) {
        requireNotNull(value) { message }
    } else {
        requireNotNull(value)
    }
}

/**
 * Represents uninitialized state.
 */
object Uninitialized : Async<Nothing>()

/**
 * Represents loading state with optional progress rate.
 */
data class Loading(
    /*@FloatRange(from = 0.0, to = 1.0)*/
    val progress: Float? = null,
) : Async<Nothing>()

/**
 * Stores successfully loaded [value].
 */
data class Success<out T>(override val value: T) : Async<T>()

/**
 * Represents failure when loading the [value] and stores corresponding [error].
 */
data class Fail<out T>(val error: Throwable) : Async<T>()
