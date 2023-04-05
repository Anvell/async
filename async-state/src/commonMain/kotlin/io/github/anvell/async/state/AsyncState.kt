package io.github.anvell.async.state

import io.github.anvell.async.Async
import io.github.anvell.async.Fail
import io.github.anvell.async.Loading
import io.github.anvell.async.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1

/**
 * Set of operators for UDF state management based on [Async].
 */
public interface AsyncState<S> {

    /**
     * Delegate implementation of [AsyncState] based on [MutableStateFlow].
     */
    public class Delegate<S>(initialState: S) : AsyncState<S> {
        override val stateFlow: MutableStateFlow<S> = MutableStateFlow(initialState)
    }

    /**
     * [MutableStateFlow] with current state.
     */
    public val stateFlow: MutableStateFlow<S>

    /**
     * Provide current state in [block]
     */
    public fun withState(block: (state: S) -> Unit): Unit = block(stateFlow.value)

    /**
     * Update current state with [reducer].
     */
    public fun setState(reducer: S.() -> S) {
        stateFlow.value = reducer(stateFlow.value)
    }

    /**
     * Subscribe for [property] changes in state.
     */
    public fun <P> CoroutineScope.selectSubscribe(
        property: KProperty1<S, P>,
        block: (P) -> Unit
    ): Job = launch {
        stateFlow
            .map(property::get)
            .distinctUntilChanged()
            .collect(block)
    }

    /**
     * Collect flow of values wrapped in `Async` and update state with [reducer].
     */
    public fun <V> Flow<Async<V>>.collectAsyncAsState(
        scope: CoroutineScope,
        initialState: Async<V>? = Loading(),
        reducer: S.(Async<V>) -> S
    ): Job = scope.launch {
        if (initialState != null) {
            setState { reducer(initialState) }
        }
        catch { setState { reducer(Fail(it)) } }
            .collect { setState { reducer(it) } }
    }

    /**
     * Collect flow of values wrapped in `Result` and update state with [reducer].
     */
    public fun <V> Flow<Result<V>>.collectReduceAsState(
        scope: CoroutineScope,
        initialState: Async<V>? = Loading(),
        reducer: S.(Async<V>) -> S
    ): Job = scope.launch {
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
     * Collect flow of values and update state with [reducer].
     */
    public fun <V> Flow<V>.collectAsState(
        scope: CoroutineScope,
        initialState: Async<V>? = Loading(),
        reducer: S.(Async<V>) -> S
    ): Job = scope.launch {
        if (initialState != null) {
            setState { reducer(initialState) }
        }
        catch { setState { reducer(Fail(it)) } }
            .collect { setState { reducer(Success(it)) } }
    }

    /**
     * Await [result][Result] and update state with [reducer].
     */
    public fun <V> ScopedDeferred<Result<V>>.reduceAsState(
        initialState: Async<V>? = Loading(),
        reducer: S.(Async<V>) -> S
    ): Job = let { (scope, value) ->
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
    public fun <V> ScopedDeferred<V>.catchAsState(
        initialState: Async<V>? = Loading(),
        reducer: S.(Async<V>) -> S
    ): Job = let { (scope, value) ->
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
