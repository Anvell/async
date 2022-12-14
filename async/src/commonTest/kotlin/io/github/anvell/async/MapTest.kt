package io.github.anvell.async

import io.github.anvell.async.mock.MockValue
import kotlin.test.Test
import kotlin.test.assertEquals

class MapTest {

    class UninitializedState {

        @Test
        fun map() {
            assertEquals(Uninitialized.map { MockValue }, Uninitialized)
        }
    }

    class LoadingState {

        private val mockProgress = 0.3f

        @Test
        fun withoutProgress() {
            assertEquals(Loading().map { MockValue }, Loading())
        }

        @Test
        fun withProgress() {
            assertEquals(Loading(mockProgress).map { MockValue }, Loading(mockProgress))
        }
    }

    class SuccessState {

        @Test
        fun map() {
            assertEquals(Success("initial value").map { MockValue }, Success(MockValue))
        }
    }

    class FailState {

        private val mockError = IllegalArgumentException()

        @Test
        fun map() {
            assertEquals(Fail<String>(mockError).map { MockValue }, Fail(mockError))
        }
    }
}
