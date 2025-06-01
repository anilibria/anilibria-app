package ru.radiationx.shared.ktx

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlin.coroutines.cancellation.CancellationException

suspend fun <P, R> List<P>.sequentialFirstNotFailure(block: suspend (P) -> R): R {
    return firstNotFailure { params ->
        var result: Result<R>? = null
        params.firstOrNull { param ->
            val paramResult = coRunCatching {
                block.invoke(param)
            }
            result = paramResult
            paramResult.isSuccess
        }
        result
    }
}

suspend fun <P, R> List<P>.parallelFirstNotFailure(block: suspend (P) -> R): R {
    return firstNotFailure { params ->
        var result: Result<R>? = null

        val paramsFlows = params.map { param ->
            flow {
                val paramResult = coRunCatching {
                    block.invoke(param)
                }
                emit(paramResult)
            }
        }

        paramsFlows.merge().firstOrNull {
            result = it
            it.isSuccess
        }
        result
    }
}

private suspend fun <P, R> List<P>.firstNotFailure(
    block: suspend (List<P>) -> Result<R>?
): R {
    val params = this
    check(params.isNotEmpty()) {
        "Params is empty"
    }
    val result: Result<R>? = block.invoke(params)
    checkNotNull(result) {
        "firstNotFailure returns null result"
    }
    val exception = result.exceptionOrNull()
    if (exception != null && exception !is CancellationException) {
        throw IllegalStateException("firstNotFailure failed with last exception:", exception)
    }
    return result.getOrThrow()
}