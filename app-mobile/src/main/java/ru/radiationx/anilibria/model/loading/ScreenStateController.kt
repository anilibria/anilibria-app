package ru.radiationx.anilibria.model.loading

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

class ScreenStateController<T : Any>(defaultState: T) {

    private val stateRelay = BehaviorRelay.createDefault<T>(defaultState)

    var currentState: T
        get() = requireNotNull(stateRelay.value)
        private set(value) {
            stateRelay.accept(value)
        }

    fun updateState(block: (T) -> T) {
        currentState = block.invoke(currentState)
    }

    fun observeState(): Observable<T> {
        return stateRelay.hide().distinctUntilChanged()
    }
}