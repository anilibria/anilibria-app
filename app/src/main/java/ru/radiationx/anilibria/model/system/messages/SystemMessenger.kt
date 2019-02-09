package ru.radiationx.anilibria.model.system.messages

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import javax.inject.Inject

class SystemMessenger @Inject constructor() {
    private val messagesRelay = PublishRelay.create<SystemMessage>()

    fun observe(): Observable<SystemMessage> = messagesRelay.hide()

    fun showMessage(message: String) = messagesRelay.accept(SystemMessage(message))
    fun showMessage(message: SystemMessage) = messagesRelay.accept(message)
}