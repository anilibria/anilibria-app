package ru.radiationx.media.mobile.controllers.gesture

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LongTapSeeker {

    private val _state = MutableStateFlow(false)
    val state = _state.asStateFlow()

    fun onOnLongTap() {
        _state.value = true
    }

    fun onTouchEnd() {
        _state.value = false
    }
}