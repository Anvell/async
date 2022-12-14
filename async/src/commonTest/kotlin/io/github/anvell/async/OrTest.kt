package io.github.anvell.async

import io.github.anvell.async.mock.MockValue
import kotlin.test.Test
import kotlin.test.assertEquals

class OrTest {

    class UninitializedState {

        @Test
        fun or() {
            assertEquals(Uninitialized.or { _ -> Success(MockValue) }, Uninitialized)
        }
    }

    class LoadingState {

        private val mockProgress = 0.3f

        @Test
        fun withoutProgress() {
            assertEquals(Loading().or { _ -> Success(MockValue) }, Loading())
        }

        @Test
        fun withProgress() {
            assertEquals(Loading(mockProgress).or { _ -> Success(MockValue) }, Loading(mockProgress))
        }
    }

    class SuccessState {

        private val mockInitialValue = "initial value"

        @Test
        fun or() {
            assertEquals(Success(mockInitialValue).or { _ -> Success(MockValue) }, Success(mockInitialValue))
        }
    }

    class FailState {

        private val mockError = IllegalArgumentException()

        @Test
        fun or() {
            assertEquals(Fail<String>(mockError).or { Success(MockValue) }, Success(MockValue))
        }
    }
}
