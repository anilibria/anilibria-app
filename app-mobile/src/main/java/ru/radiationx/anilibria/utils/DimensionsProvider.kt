package ru.radiationx.anilibria.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * Created by radiationx on 09.01.18.
 */
class DimensionsProvider @Inject constructor() {
    private val relay = MutableStateFlow(Dimensions())
    fun observe(): Flow<Dimensions> = relay
    fun get() = relay.value

    fun update(dimensions: Dimensions) {
        relay.value = dimensions
    }
}