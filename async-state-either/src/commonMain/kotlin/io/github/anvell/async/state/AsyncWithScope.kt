package io.github.anvell.async.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Creates a coroutine and returns its future result same as [async] together with [CoroutineScope].
 *
 * @see [async]
 */
public fun <T> CoroutineScope.asyncWithScope(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Pair<CoroutineScope, Deferred<T>> = this to async(context, start, block)
