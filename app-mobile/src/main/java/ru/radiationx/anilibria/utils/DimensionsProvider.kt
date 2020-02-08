package ru.radiationx.anilibria.utils

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by radiationx on 09.01.18.
 */
class DimensionsProvider @Inject constructor() {
    private val relay = BehaviorRelay.createDefault(DimensionHelper.Dimensions())
    fun observe(): Observable<DimensionHelper.Dimensions> = relay
    fun get() = relay.value!!

    fun update(dimensions: DimensionHelper.Dimensions) {
        relay.accept(dimensions)
    }
}