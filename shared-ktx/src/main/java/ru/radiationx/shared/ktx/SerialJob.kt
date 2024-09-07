package ru.radiationx.shared.ktx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SerialJob {

    private var currentJob: Job? = null

    fun launch(
        coroutineScope: CoroutineScope,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val job = coroutineScope.launch(context, start, block)
        set(job)
    }

    fun cancel() {
        currentJob?.cancel()
    }

    private fun set(job: Job) {
        cancel()
        currentJob = job
    }
}