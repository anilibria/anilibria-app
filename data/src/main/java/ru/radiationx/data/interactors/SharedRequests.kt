package ru.radiationx.data.interactors

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.radiationx.shared.ktx.coRunCatching

class SharedRequests<KEY, DATA> {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val requestEvent = MutableSharedFlow<Pair<KEY, Result<DATA>>>()

    private val jobs = mutableMapOf<KEY, Job>()

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun request(key: KEY, block: suspend () -> DATA): DATA {
        if (jobs[key]?.isActive != true) {
            val job = scope.launch {
                val result = coRunCatching {
                    block.invoke()
                }
                requestEvent.emit(key to result)
            }
            jobs[key] = job
            job.invokeOnCompletion { error ->
                if (error != null) {
                    GlobalScope.launch {
                        requestEvent.emit(key to Result.failure(error))
                    }
                }
            }
        }
        return requestEvent
            .filter { it.first == key }
            .map { it.second }
            .first()
            .getOrThrow()
    }
}