package io.github.anvell.async.state

import io.github.anvell.async.Async
import io.github.anvell.async.Fail
import io.github.anvell.async.Loading
import io.github.anvell.async.Success
import io.github.anvell.async.Uninitialized
import io.github.anvell.async.state.mock.MockData
import io.github.anvell.async.state.mock.MockException
import io.github.anvell.either.Either
import io.github.anvell.either.Left
import io.github.anvell.either.Right
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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
        withState { state ->
            assertEquals(state.text, Uninitialized)
        }
        setState { copy(text = Success("some text")) }
        withState { state ->
            assertEquals(state.text, Success("some text"))
        }
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
                Success("bar"),
            ),
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
                Fail(MockException),
            ),
        )
    }

    @Test
    fun collectFlowWithEitherAsState() = runTest {
        val flow: Flow<Either<MockException, String>> = flow {
            delay(1)
            emit(Right("foo"))
            delay(1)
            emit(Left(MockException))
            delay(1)
            emit(Right("bar"))
        }

        val received = mutableListOf<Async<String>>()
        val selectSubscribe = selectSubscribe(MockData::text, received::add)
        val collectReduceAsState = flow.collectReduceAsState(this) { copy(text = it) }
        collectReduceAsState.join()
        selectSubscribe.cancel()

        assertEquals(
            expected = received,
            actual = listOf(
                Uninitialized,
                Loading(),
                Success("foo"),
                Fail(MockException),
                Success("bar"),
            ),
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
        val collectAsyncAsState = flow
            .collectAsyncAsState(
                initialState = Loading(0f),
                scope = this,
            ) { copy(text = it) }
        collectAsyncAsState.join()
        selectSubscribe.cancel()

        assertEquals(
            expected = received,
            actual = listOf(
                Uninitialized,
                Loading(progress = 0f),
                Loading(progress = 0.3f),
                Loading(progress = 0.6f),
                Success("foo"),
            ),
        )
    }

    @Test
    fun reducingSuccessValueAsState() = runTest {
        val received = mutableListOf<Async<String>>()
        val selectSubscribe = selectSubscribe(MockData::text, received::add)
        val asyncScoped = asyncWithScope {
            delay(1)
            Right("some text")
        }.reduceAsState { copy(text = it) }
        asyncScoped.join()
        selectSubscribe.cancel()

        assertEquals(
            expected = received,
            actual = listOf(
                Uninitialized,
                Loading(),
                Success("some text"),
            ),
        )
    }

    @Test
    fun reducingErrorValueAsState() = runTest {
        val received = mutableListOf<Async<String>>()
        val selectSubscribe = selectSubscribe(MockData::text, received::add)
        val asyncScoped = asyncWithScope {
            delay(1)
            Left(MockException)
        }.reduceAsState { copy(text = it) }
        asyncScoped.join()
        selectSubscribe.cancel()

        assertEquals(
            expected = received,
            actual = listOf(
                Uninitialized,
                Loading(),
                Fail(MockException),
            ),
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
                Success("some text"),
            ),
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
                Fail(MockException),
            ),
        )
    }
}
