package ru.radiationx.anilibria.ui.common

import ru.radiationx.anilibria.model.data.BlazingFastException
import ru.radiationx.anilibria.model.data.GoogleCaptchaException
import ru.radiationx.anilibria.model.system.messages.SystemMessenger
import ru.radiationx.anilibria.presentation.common.IErrorHandler

/**
 * Created by radiationx on 23.02.18.
 */
class ErrorHandler(
        private val systemMessenger: SystemMessenger
) : IErrorHandler {

    override fun handle(throwable: Throwable, messageListener: ((Throwable, String?) -> Unit)?) {
        throwable.printStackTrace()
        val message = getMessage(throwable)
        if (messageListener != null) {
            messageListener.invoke(throwable, message)
        } else {
            systemMessenger.showMessage(message)
        }
    }

    private fun getMessage(throwable: Throwable): String {
        if (throwable is GoogleCaptchaException || throwable is BlazingFastException) {
            return "Защита от DDOS: ${throwable.message}"
        }
        return throwable.message.orEmpty()
    }
}