package ru.radiationx.shared.ktx

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class EventFlow<T> : Flow<T> {

    private val flow = MutableStateFlow<Event<T>?>(null)

    fun observe(): Flow<T> = flow
        .filterNotNull()
        .mapEvent()

    fun set(value: T) {
        flow.value = Event(value)
    }

    fun emit(value: T) {
        set(value)
    }

    override suspend fun collect(collector: FlowCollector<T>) {
        return observe().collect(collector)
    }
}