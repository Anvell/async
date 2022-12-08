package io.github.anvell.async

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe

internal class AsyncSpec : DescribeSpec({
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
                async.value shouldBe null
            }
        }

        context("when value is loading") {
            beforeEach {
                async = Loading
            }

            it("should not return any value") {
                async.value shouldBe null
            }
        }

        context("when value has been successfully loaded") {
            beforeEach {
                async = Success("FooBar")
            }

            it("should return actual value") {
                async.value shouldBe "FooBar"
            }
        }

        context("when loading value has failed") {
            beforeEach {
                async = Fail(Throwable())
            }

            it("should not return any value") {
                async.value shouldBe null
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

internal class FailSpec : DescribeSpec({
    lateinit var fail: Fail<String>

    beforeEach {
        fail = Fail(NullPointerException())
    }

    describe("obtaining an error") {
        it("should return actual error") {
            fail.error shouldBe NullPointerException()
        }
    }
})
