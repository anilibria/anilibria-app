package ru.radiationx.shared.ktx

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

fun <T> Flow<Event<T>?>.onEachEvent(action: suspend (T) -> Unit): Flow<Event<T>> {
    return filterNotNull().onEach { event ->
        event.content()?.let { action(it) }
    }
}

fun <T> Flow<Event<T>?>.mapEvent(): Flow<T> {
    return filterNotNull().mapNotNull {
        it.content()
    }
}