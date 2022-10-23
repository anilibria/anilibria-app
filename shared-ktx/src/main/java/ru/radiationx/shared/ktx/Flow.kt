package ru.radiationx.shared.ktx

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


fun <T> Flow<T>.repeatWhen(repeat: Flow<*>): Flow<T> {
    return flow {
        collect { emit(it) }
        repeat.collect {
            collect { emit(it) }
        }
    }
}