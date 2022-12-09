package io.github.anvell.async.state

import io.github.anvell.async.Async
import io.github.anvell.async.Fail
import io.github.anvell.async.Loading
import io.github.anvell.async.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1

interface AsyncState<S> {
    val stateFlow: MutableStateFlow<S>

    fun <V> withState(block: (S) -> V) = block(stateFlow.value)

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

    fun <V> Flow<Result<V>>.collectReduceAsState(
        scope: CoroutineScope,
        initialState: Async<V>? = Loading,
        reducer: S.(Async<V>) -> S
    ) = scope.launch {
        if (initialState != null) {
            setState { reducer(initialState) }
        }
        catch { setState { reducer(Fail(it)) } }
            .collect {
                it.fold(
                    onFailure = { setState { reducer(Fail(it)) } },
                    onSuccess = { setState { reducer(Success(it)) } }
                )
            }
    }

    fun <V> Flow<V>.collectAsState(
        scope: CoroutineScope,
        initialState: Async<V>? = Loading,
        reducer: S.(Async<V>) -> S
    ) = scope.launch {
        if (initialState != null) {
            setState { reducer(initialState) }
        }
        catch { setState { reducer(Fail(it)) } }
            .collect { setState { reducer(Success(it)) } }
    }

    fun <V> ScopedDeferred<Result<V>>.reduceAsState(
        initialState: Async<V>? = Loading,
        reducer: S.(Async<V>) -> S
    ) = let { (scope, value) ->
        scope.launch {
            if (initialState != null) {
                setState { reducer(initialState) }
            }
            value.await().fold(
                onFailure = { setState { reducer(Fail(it)) } },
                onSuccess = { setState { reducer(Success(it)) } }
            )
        }
    }

    fun <V> ScopedDeferred<V>.catchAsState(
        initialState: Async<V>? = Loading,
        reducer: S.(Async<V>) -> S
    ) = let { (scope, value) ->
        scope.launch {
            if (initialState != null) {
                setState { reducer(initialState) }
            }
            try {
                val result = value.await()
                setState { reducer(Success(result)) }
            } catch (error: Throwable) {
                setState { reducer(Fail(error)) }
            }
        }
    }
}
