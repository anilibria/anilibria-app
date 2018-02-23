package ru.radiationx.anilibria.presentation

import android.content.Context
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 23.02.18.
 */
class ErrorHandlerImpl(
        private val context: Context,
        private val router: Router
) : ErrorHandler {

    override fun handle(throwable: Throwable,  messageListener: ((Throwable, String?) -> Unit)?) {
        throwable.printStackTrace()
        val message = getMessage(throwable)
        if (messageListener != null) {
            messageListener.invoke(throwable, message)
        } else {
            router.showSystemMessage(message)
        }
    }

    private fun getMessage(throwable: Throwable): String {
        return throwable.message.orEmpty()
    }
}