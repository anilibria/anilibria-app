package ru.radiationx.anilibria.ui.common

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.datasource.remote.ApiError
import ru.radiationx.data.system.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

/**
 * Created by radiationx on 23.02.18.
 */
class ErrorHandler @Inject constructor(
    private val systemMessenger: SystemMessenger,
) : IErrorHandler {

    override fun handle(throwable: Throwable, messageListener: ((Throwable, String?) -> Unit)?) {
        Timber.e(throwable)
        val message = getMessage(throwable)
        if (messageListener != null) {
            messageListener.invoke(throwable, message)
        } else {
            systemMessenger.showMessage(message)
        }
    }

    private fun getMessage(throwable: Throwable) = when (throwable) {
        is IOException -> "Нет соединения с интернетом"
        is HttpException -> throwable.message
        is ApiError -> throwable.userMessage()
        is JsonDataException -> "Неправильный формат данных"
        else -> throwable.message ?: "Неизвестная ошибка"
    }

    private fun ApiError.userMessage() = when {
        !message.isNullOrBlank() -> message.orEmpty()
        !description.isNullOrBlank() -> description.orEmpty()
        else -> "Неизвестная ошибка"
    }
}