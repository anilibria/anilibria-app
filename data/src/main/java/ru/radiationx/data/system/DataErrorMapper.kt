package ru.radiationx.data.system

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import retrofit2.HttpException
import ru.radiationx.data.datasource.remote.ApiError
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import java.security.GeneralSecurityException
import javax.inject.Inject

class DataErrorMapper @Inject constructor() {

    fun handle(throwable: Throwable): String? {
        return when (throwable) {
            is SocketTimeoutException,
            is SocketException,
            is UnknownServiceException,
            is GeneralSecurityException -> throwable.extractExceptionName()

            is HttpException -> throwable.message
            is ApiError -> throwable.userMessage()
            is UnknownHostException -> "Нет соединения с интернетом"

            is JsonDataException -> "Json: ${throwable.message}"
            is JsonEncodingException -> "Json: ${throwable.message}"
            is IOException -> "IO: ${throwable.extractExceptionName()}: ${throwable.message}"
            else -> null
        }
    }

    private inline fun <reified T : Throwable> T.extractExceptionName(): String? {
        return this::class.simpleName?.removeSuffix("Exception")
    }

    private fun ApiError.userMessage() = when {
        !message.isNullOrBlank() -> message.orEmpty()
        !description.isNullOrBlank() -> description.orEmpty()
        else -> "Неизвестная ошибка"
    }
}