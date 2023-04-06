package io.github.anvell.async.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

public typealias ScopedDeferred<T> = Pair<CoroutineScope, Deferred<T>>
