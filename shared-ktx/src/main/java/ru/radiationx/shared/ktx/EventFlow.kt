package ru.radiationx.shared.ktx

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class EventFlow<T> {

    private val flow = MutableStateFlow<Event<T>?>(null)

    fun observe(): Flow<T> = flow
        .filterNotNull()
        .mapEvent()

    fun set(value: T) {
        flow.value = Event(value)
    }
}