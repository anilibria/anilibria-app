package ru.radiationx.shared.ktx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SerialJob {

    private var currentJob: Job? = null

    fun set(job: Job) {
        currentJob?.cancel()
        currentJob = job
    }

    fun get(): Job? = currentJob

    fun cancel() {
        currentJob?.takeIf { !it.isCancelled }?.cancel()
    }

    fun launch(
        coroutineScope: CoroutineScope,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val job = coroutineScope.launch(context, start, block)
        set(job)
    }
}