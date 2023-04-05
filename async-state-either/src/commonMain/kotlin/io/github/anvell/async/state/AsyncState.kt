package io.github.anvell.async.state

import io.github.anvell.async.Async
import io.github.anvell.async.Fail
import io.github.anvell.async.Loading
import io.github.anvell.async.Success
import io.github.anvell.either.Either
import io.github.anvell.either.fold
import kotlinx.coroutines.CoroutineScope
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
    fun withState(block: (state: S) -> Unit) = block(stateFlow.value)

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
     * Collect flow of values wrapped in `Async` and update state with [reducer].
     */
    fun <V> Flow<Async<V>>.collectAsyncAsState(
        scope: CoroutineScope,
        initialState: Async<V>? = Loading(),
        reducer: S.(Async<V>) -> S
    ) = scope.launch {
        if (initialState != null) {
            setState { reducer(initialState) }
        }
        catch { setState { reducer(Fail(it)) } }
            .collect { setState { reducer(it) } }
    }

    /**
     * Collect flow of values wrapped in `Either` and update state with [reducer].
     */
    fun <L, R> Flow<Either<L, R>>.collectReduceAsState(
        scope: CoroutineScope,
        initialState: Async<R>? = Loading(),
        reducer: S.(Async<R>) -> S
    ) where L : Throwable = scope.launch {
        if (initialState != null) {
            setState { reducer(initialState) }
        }
        catch { setState { reducer(Fail(it)) } }
            .collect {
                it.fold(
                    left = { setState { reducer(Fail(it)) } },
                    right = { setState { reducer(Success(it)) } }
                )
            }
    }

    /**
     * Collect flow of values and update state with [reducer].
     */
    fun <V> Flow<V>.collectAsState(
        scope: CoroutineScope,
        initialState: Async<V>? = Loading(),
        reducer: S.(Async<V>) -> S
    ) = scope.launch {
        if (initialState != null) {
            setState { reducer(initialState) }
        }
        catch { setState { reducer(Fail(it)) } }
            .collect { setState { reducer(Success(it)) } }
    }

    /**
     * Await [either][Either] and update state with [reducer].
     */
    fun <L, R> ScopedDeferred<Either<L, R>>.reduceAsState(
        initialState: Async<R>? = Loading(),
        reducer: S.(Async<R>) -> S
    ) where L : Throwable = let { (scope, value) ->
        scope.launch {
            if (initialState != null) {
                setState { reducer(initialState) }
            }
            value.await().fold(
                left = { setState { reducer(Fail(it)) } },
                right = { setState { reducer(Success(it)) } }
            )
        }
    }

    /**
     * Await value and update state with [reducer].
     */
    fun <V> ScopedDeferred<V>.catchAsState(
        initialState: Async<V>? = Loading(),
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
