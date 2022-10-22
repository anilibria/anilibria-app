package ru.radiationx.anilibria.utils.messages

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

// todo tr-274 check working
class SystemMessenger @Inject constructor() {

    private val messagesRelay = MutableSharedFlow<SystemMessage>()

    fun observe(): Flow<SystemMessage> = messagesRelay

    fun showMessage(message: String) = runBlocking {
        messagesRelay.emit(SystemMessage(message))
    }

    fun showMessage(message: SystemMessage) = runBlocking {
        messagesRelay.emit(message)
    }
}