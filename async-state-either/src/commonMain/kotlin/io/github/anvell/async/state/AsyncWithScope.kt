package io.github.anvell.async.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Creates a coroutine and returns its future result same as [async] together with [CoroutineScope].
 *
 * @see [async]
 */
fun <T> CoroutineScope.asyncWithScope(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
) = this to async(context, start, block)
