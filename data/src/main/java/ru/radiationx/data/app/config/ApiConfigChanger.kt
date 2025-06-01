package ru.radiationx.data.app.config

import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

class ApiConfigChanger @Inject constructor(
) {
    private val relay = MutableSharedFlow<Unit>()
    fun observeConfigChanges() = relay
    suspend fun onChange() {
        relay.emit(Unit)
    }

}