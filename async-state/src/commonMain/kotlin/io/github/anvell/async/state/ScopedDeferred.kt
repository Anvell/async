package io.github.anvell.async.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

internal typealias ScopedDeferred<T> = Pair<CoroutineScope, Deferred<T>>
