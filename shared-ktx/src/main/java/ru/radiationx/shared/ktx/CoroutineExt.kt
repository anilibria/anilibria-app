package ru.radiationx.shared.ktx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeoutException
import kotlin.coroutines.cancellation.CancellationException

inline fun <T, R> T.coRunCatching(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        if (e is CancellationException) {
            throw e
        } else {
            Result.failure(e)
        }
    }
}

suspend fun <T> withTimeoutOrThrow(timeMillis: Long, block: suspend CoroutineScope.() -> T): T {
    return runCatching {
        withTimeout(timeMillis, block)
    }.getOrElse {
        if (it is TimeoutCancellationException) {
            throw TimeoutException(it.message).apply {
                initCause(it)
            }
        } else {
            throw it
        }
    }
}