package ru.radiationx.anilibria.ui.common

import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.network.DataErrorMapper
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by radiationx on 23.02.18.
 */
class ErrorHandler @Inject constructor(
    private val systemMessenger: SystemMessenger,
    private val dataErrorMapper: DataErrorMapper
) : IErrorHandler {

    override fun handle(throwable: Throwable, messageListener: ((Throwable, String?) -> Unit)?) {
        Timber.e(throwable)
        val message = dataErrorMapper.handle(throwable) ?: getMessage(throwable)
        if (messageListener != null) {
            messageListener.invoke(throwable, message)
        } else {
            systemMessenger.showMessage(message)
        }
    }

    private fun getMessage(throwable: Throwable): String {
        return "Неизвестная ошибка: $throwable"
    }
}