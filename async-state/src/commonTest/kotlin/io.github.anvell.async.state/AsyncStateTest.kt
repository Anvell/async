package io.github.anvell.async.state

import io.github.anvell.async.*
import io.github.anvell.async.state.mock.MockData
import io.github.anvell.async.state.mock.MockException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AsyncStateTest : AsyncState<MockData> by AsyncState.Delegate(MockData()) {

    @BeforeTest
    fun setUp() {
        setState { MockData() }
    }

    @Test
    fun changeStateDirectly() {
        assertEquals(withState(MockData::text), Uninitialized)
        setState { copy(text = Success("some text")) }
        assertEquals(withState(MockData::text), Success("some text"))
    }

    @Test
    fun collectFlowWithValuesAsState() = runTest {
        val flow = flow {
            delay(1)
            emit("foo")
            delay(1)
            emit("foo")
            delay(1)
            emit("bar")
        }

        val received = mutableListOf<Async<String>>()
        val selectSubscribe = selectSubscribe(MockData::text, received::add)
        val collectAsState = flow.collectAsState(this) { copy(text = it) }
        collectAsState.join()
        selectSubscribe.cancel()

        assertEquals(
            expected = received,
            actual = listOf(
                Uninitialized,
                Loading(),
                Success("foo"),
                Success("bar")
            )
        )
    }

    @Test
    fun collectFlowWithErrorAsState() = runTest {
        val flow = flow {
            delay(1)
            emit("foo")
            delay(1)
            throw MockException
        }

        val received = mutableListOf<Async<String>>()
        val selectSubscribe = selectSubscribe(MockData::text, received::add)
        val collectAsState = flow.collectAsState(this) { copy(text = it) }
        collectAsState.join()
        selectSubscribe.cancel()

        assertEquals(
            expected = received,
            actual = listOf(
                Uninitialized,
                Loading(),
                Success("foo"),
                Fail(MockException)
            )
        )
    }

    @Test
    fun collectFlowWithResultsAsState() = runTest {
        val flow = flow {
            delay(1)
            emit(Result.success("foo"))
            delay(1)
            emit(Result.failure(MockException))
            delay(1)
            emit(Result.success("bar"))
        }

        val received = mutableListOf<Async<String>>()
        val selectSubscribe = selectSubscribe(MockData::text, received::add)
        val collectAsState = flow
            .collectReduceAsState(
                scope = this,
                initialState = Success("initial state")
            ) { copy(text = it) }
        collectAsState.join()
        selectSubscribe.cancel()

        assertEquals(
            expected = received,
            actual = listOf(
                Uninitialized,
                Success("initial state"),
                Success("foo"),
                Fail(MockException),
                Success("bar")
            )
        )
    }

    @Test
    fun collectFlowWithProgressAsState() = runTest {
        val flow: Flow<Async<String>> = flow {
            delay(1)
            emit(Loading(0.3f))
            delay(1)
            emit(Loading(0.6f))
            delay(1)
            emit(Success("foo"))
        }

        val received = mutableListOf<Async<String>>()
        val selectSubscribe = selectSubscribe(MockData::text, received::add)
        val collectAsState = launch {
            flow.collect { setState { copy(text = it) } }
        }
        collectAsState.join()
        selectSubscribe.cancel()

        assertEquals(
            expected = received,
            actual = listOf(
                Uninitialized,
                Loading(progress = 0.3f),
                Loading(progress = 0.6f),
                Success("foo")
            )
        )
    }

    @Test
    fun reducingSuccessValueAsState() = runTest {
        val received = mutableListOf<Async<String>>()
        val selectSubscribe = selectSubscribe(MockData::text, received::add)
        val asyncScoped = asyncWithScope {
            delay(1)
            Result.success("some text")
        }.reduceAsState { copy(text = it) }
        asyncScoped.join()
        selectSubscribe.cancel()

        assertEquals(
            expected = received,
            actual = listOf(
                Uninitialized,
                Loading(),
                Success("some text")
            )
        )
    }

    @Test
    fun reducingErrorValueAsState() = runTest {
        val received = mutableListOf<Async<String>>()
        val selectSubscribe = selectSubscribe(MockData::text, received::add)
        val asyncScoped = asyncWithScope<Result<String>> {
            delay(1)
            Result.failure(MockException)
        }.reduceAsState { copy(text = it) }
        asyncScoped.join()
        selectSubscribe.cancel()

        assertEquals(
            expected = received,
            actual = listOf(
                Uninitialized,
                Loading(),
                Fail(MockException)
            )
        )
    }

    @Test
    fun catchingValueAsState() = runTest {
        val received = mutableListOf<Async<String>>()
        val selectSubscribe = selectSubscribe(MockData::text, received::add)
        val asyncScoped = asyncWithScope {
            delay(1)
            "some text"
        }.catchAsState { copy(text = it) }
        asyncScoped.join()
        selectSubscribe.cancel()

        assertEquals(
            expected = received,
            actual = listOf(
                Uninitialized,
                Loading(),
                Success("some text")
            )
        )
    }

    @Test
    fun catchingErrorAsState() = runTest {
        val received = mutableListOf<Async<String>>()
        val selectSubscribe = selectSubscribe(MockData::text, received::add)
        val asyncScoped = asyncWithScope(SupervisorJob()) {
            delay(1)
            throw MockException
        }.catchAsState { copy(text = it) }
        asyncScoped.join()
        selectSubscribe.cancel()

        assertEquals(
            expected = received,
            actual = listOf(
                Uninitialized,
                Loading(),
                Fail(MockException)
            )
        )
    }
}
