package ru.radiationx.anilibria.presentation

import android.content.Context
import ru.radiationx.anilibria.model.data.BlazingFastException
import ru.radiationx.anilibria.model.data.GoogleCaptchaException
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 23.02.18.
 */
class ErrorHandler(
        private val context: Context,
        private val router: Router
) : IErrorHandler {

    override fun handle(throwable: Throwable, messageListener: ((Throwable, String?) -> Unit)?) {
        throwable.printStackTrace()
        val message = getMessage(throwable)
        if (messageListener != null) {
            messageListener.invoke(throwable, message)
        } else {
            router.showSystemMessage(message)
        }
    }

    private fun getMessage(throwable: Throwable): String {
        if (throwable is GoogleCaptchaException || throwable is BlazingFastException) {
            return "Защита от DDOS: ${throwable.message}"
        }
        return throwable.message.orEmpty()
    }
}