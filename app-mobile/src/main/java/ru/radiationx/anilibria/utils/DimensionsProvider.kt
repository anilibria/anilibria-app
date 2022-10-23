package ru.radiationx.anilibria.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * Created by radiationx on 09.01.18.
 */
class DimensionsProvider @Inject constructor() {
    private val relay = MutableStateFlow(DimensionHelper.Dimensions())
    fun observe(): Flow<DimensionHelper.Dimensions> = relay
    fun get() = relay.value

    fun update(dimensions: DimensionHelper.Dimensions) {
        relay.value = dimensions
    }
}