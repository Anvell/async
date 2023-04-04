package io.github.anvell.async.state.mock

import io.github.anvell.async.Async
import io.github.anvell.async.Uninitialized

data class MockData(
    val text: Async<String> = Uninitialized,
)
