package io.github.anvell.async.state

import io.github.anvell.async.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.reflect.KProperty1

/**
 * Set of operators for UDF state management based on [Async].
 */
interface AsyncState<S> {

    /**
     * Delegate implementation of [AsyncState] based on [MutableStateFlow].
     */
    class Delegate<S>(initialState: S) : AsyncState<S> {
        override val stateFlow = MutableStateFlow(initialState)
    }

    /**
     * [MutableStateFlow] with current state.
     */
    val stateFlow: MutableStateFlow<S>

    /**
     * Provide current state in [block]
     */
    fun <V> withState(block: (S) -> V) = block(stateFlow.value)

    /**
     * Update current state with [reducer].
     */
    fun setState(reducer: S.() -> S) {
        stateFlow.value = reducer(stateFlow.value)
    }

    /**
     * Subscribe for [property] changes in state.
     */
    fun <P> CoroutineScope.selectSubscribe(
        property: KProperty1<S, P>,
        block: (P) -> Unit
    ) = launch {
        stateFlow
            .map(property::get)
            .distinctUntilChanged()
            .collect(block)
    }

    /**
     * Collect flow and update state with [reducer].
     */
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

    /**
     * Collect flow and update state with [reducer].
     */
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

    /**
     * Await [result][Result] and update state with [reducer].
     */
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

    /**
     * Await value and update state with [reducer].
     */
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
