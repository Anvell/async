package io.github.anvell.async

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe

internal class AsyncSpec: DescribeSpec({
    lateinit var async: Async<String>

    it("should have only four possible states") {
        Async::class.sealedSubclasses.size shouldBe 4
        Async::class.sealedSubclasses shouldContainAll listOf(
            Uninitialized::class,
            Loading::class,
            Success::class,
            Fail::class
        )
    }

    describe("invoking") {
        context("when value is uninitialized") {
            beforeEach {
                async = Uninitialized
            }

            it("should not return any value") {
                async() shouldBe null
            }
        }

        context("when value is loading") {
            beforeEach {
                async = Loading
            }

            it("should not return any value") {
                async() shouldBe null
            }
        }

        context("when value has been successfully loaded") {
            beforeEach {
                async = Success("FooBar")
            }

            it("should return actual value") {
                async() shouldBe "FooBar"
            }

            it("should consume value") {
                async()
                (async as Success).isConsumed shouldBe true
            }
        }

        context("when loading value has failed") {
            beforeEach {
                async = Fail(Throwable())
            }

            it("should not return any value") {
                async() shouldBe null
            }

            it("should not consume error") {
                async()
                (async as Fail).isConsumed shouldBe false
            }
        }
    }

    describe("unwrapping") {
        context("when value is uninitialized") {
            beforeEach {
                async = Uninitialized
            }

            it("should throw IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> { async.unwrap() }
            }
        }

        context("when value is loading") {
            beforeEach {
                async = Loading
            }

            it("should throw IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> { async.unwrap() }
            }
        }

        context("when value has been successfully loaded") {
            beforeEach {
                async = Success("FooBar")
            }

            it("should return actual value") {
                async.unwrap() shouldBe "FooBar"
            }

            it("should consume value") {
                async.unwrap()
                (async as Success).isConsumed shouldBe true
            }
        }

        context("when loading value has failed") {
            beforeEach {
                async = Fail(Throwable())
            }

            it("should throw IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> { async.unwrap() }
            }
        }
    }
})

internal class SuccessTest: DescribeSpec({
    lateinit var success: Success<String>

    beforeEach {
        success = Success("FooBar")
    }

    describe("peeking a value") {
        it("should return actual value") {
            success.peek() shouldBe "FooBar"
        }

        it("should not consume value") {
            success.peek()
            success.isConsumed shouldBe false
        }
    }
})

internal class FailTest: DescribeSpec({
    lateinit var fail: Fail<String>

    beforeEach {
        fail = Fail(NullPointerException())
    }

    describe("obtaining an error") {
        it("should return actual error") {
            fail.error shouldBe NullPointerException()
        }

        it("should consume error") {
            fail.error
            fail.isConsumed shouldBe true
        }
    }

    describe("peeking an error") {
        it("should return actual error") {
            fail.peekError() shouldBe NullPointerException()
        }

        it("should not consume error") {
            fail.peekError()
            fail.isConsumed shouldBe false
        }
    }
})
