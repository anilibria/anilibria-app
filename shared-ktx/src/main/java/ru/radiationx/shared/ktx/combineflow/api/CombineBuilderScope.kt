package ru.radiationx.combineflow.api

import kotlinx.coroutines.flow.Flow

interface CombineBuilderScope<R> {

    fun <T> Flow<T>.register(): CombineProperty<T>

    fun collect(block: suspend () -> R)
}