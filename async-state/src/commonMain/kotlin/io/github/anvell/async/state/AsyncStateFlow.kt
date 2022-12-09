package io.github.anvell.async.state

import kotlinx.coroutines.flow.MutableStateFlow

class AsyncStateFlow<S>(initialState: S) : AsyncState<S> {
    override val stateFlow = MutableStateFlow(initialState)
}
