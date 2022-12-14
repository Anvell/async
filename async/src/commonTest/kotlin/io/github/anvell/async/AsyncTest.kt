@file:Suppress("ConstPropertyName")

package io.github.anvell.async

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val DefaultErrorMessage = "Required value was null."
private const val CustomErrorMessage = "Example error message"

class AsyncTest {

    class UninitializedState {

        private val uninitialized = Uninitialized

        @Test
        fun value() {
            assertEquals(uninitialized.value, null)
        }

        @Test
        fun unwrap() {
            assertFailsWith<IllegalArgumentException>(DefaultErrorMessage) {
                uninitialized.unwrap()
            }
        }

        @Test
        fun unwrapWithMessage() {
            assertFailsWith<IllegalArgumentException>(CustomErrorMessage) {
                uninitialized.unwrap(CustomErrorMessage)
            }
        }
    }

    class LoadingState {

        private val loading = Loading(progress = .5f)

        @Test
        fun value() {
            assertEquals(loading.value, null)
        }

        @Test
        fun unwrap() {
            assertFailsWith<IllegalArgumentException>(DefaultErrorMessage) {
                loading.unwrap()
            }
        }

        @Test
        fun unwrapWithMessage() {
            assertFailsWith<IllegalArgumentException>(CustomErrorMessage) {
                loading.unwrap(CustomErrorMessage)
            }
        }
    }

    class SuccessState {

        private val mockValue = "foo"
        private val success = Success(mockValue)

        @Test
        fun value() {
            assertEquals(success.value, mockValue)
        }

        @Test
        fun unwrap() {
            assertEquals(success.unwrap(), mockValue)
        }

        @Test
        fun unwrapWithMessage() {
            assertEquals(success.unwrap(CustomErrorMessage), mockValue)
        }
    }

    class FailState {

        private val mockError = IllegalArgumentException()
        private val fail = Fail<String>(mockError)

        @Test
        fun error() {
            assertEquals(fail.error, mockError)
        }

        @Test
        fun value() {
            assertEquals(fail.value, null)
        }

        @Test
        fun unwrap() {
            assertFailsWith<IllegalArgumentException>(DefaultErrorMessage) {
                fail.unwrap()
            }
        }

        @Test
        fun unwrapWithMessage() {
            assertFailsWith<IllegalArgumentException>(CustomErrorMessage) {
                fail.unwrap(CustomErrorMessage)
            }
        }
    }
}
