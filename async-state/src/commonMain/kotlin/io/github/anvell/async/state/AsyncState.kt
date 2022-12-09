package io.github.anvell.async.state

import io.github.anvell.async.Async
import io.github.anvell.async.Fail
import io.github.anvell.async.Loading
import io.github.anvell.async.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1

interface AsyncState<S> {
    val stateFlow: MutableStateFlow<S>

    fun <T> withState(block: (S) -> T) = block(stateFlow.value)

    fun setState(reducer: S.() -> S) {
        stateFlow.value = reducer(stateFlow.value)
    }

    fun <P> CoroutineScope.selectSubscribe(
        property: KProperty1<S, P>,
        block: (P) -> Unit
    ) = launch {
        stateFlow
            .map(property::get)
            .distinctUntilChanged()
            .collect(block)
    }

    fun <V> CoroutineScope.collectReduceAsState(
        flow: Flow<Result<V>>,
        initialState: Async<V>? = Loading,
        reducer: S.(Async<V>) -> S
    ) = launch {
        if (initialState != null) {
            setState { reducer(initialState) }
        }
        flow
            .catch { setState { reducer(Fail(it)) } }
            .collect {
                it.fold(
                    onFailure = { setState { reducer(Fail(it)) } },
                    onSuccess = { setState { reducer(Success(it)) } }
                )
            }
    }

    fun <V> CoroutineScope.collectAsState(
        flow: Flow<V>,
        initialState: Async<V>? = Loading,
        reducer: S.(Async<V>) -> S
    ) = launch {
        if (initialState != null) {
            setState { reducer(initialState) }
        }
        flow
            .catch { setState { reducer(Fail(it)) } }
            .collect { setState { reducer(Success(it)) } }
    }

    fun <V> CoroutineScope.reduceAsState(
        value: suspend () -> Result<V>,
        initialState: Async<V>? = Loading,
        reducer: S.(Async<V>) -> S
    ) = launch {
        if (initialState != null) {
            setState { reducer(initialState) }
        }
        value().fold(
            onFailure = { setState { reducer(Fail(it)) } },
            onSuccess = { setState { reducer(Success(it)) } }
        )
    }

    fun <V> CoroutineScope.catchAsState(
        value: suspend () -> V,
        initialState: Async<V>? = Loading,
        reducer: S.(Async<V>) -> S
    ) = launch {
        if (initialState != null) {
            setState { reducer(initialState) }
        }
        try {
            val result = value()
            setState { reducer(Success(result)) }
        } catch (error: Throwable) {
            setState { reducer(Fail(error)) }
        }
    }

    fun <V> CoroutineScope.catchAllAsState(
        values: List<suspend () -> V>,
        initialState: Async<List<V>>? = Loading,
        reducer: S.(Async<List<V>>) -> S
    ) = launch {
        if (initialState != null) {
            setState { reducer(initialState) }
        }
        try {
            val result = values
                .map { async { it() } }
                .awaitAll()
            setState { reducer(Success(result)) }
        } catch (error: Throwable) {
            setState { reducer(Fail(error)) }
        }
    }
}
