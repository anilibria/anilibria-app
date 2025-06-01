package ru.radiationx.data.system

import anilibria.api.shared.errors.ApiErrorParser
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import java.security.GeneralSecurityException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class DataErrorMapper @Inject constructor(
    private val apiErrorParser: ApiErrorParser
) {

    fun handle(throwable: Throwable): String? {
        return when (throwable) {
            is HttpException -> userMessage(throwable)
            is UnknownHostException -> "Нет соединения с интернетом"

            is JsonDataException,
            is JsonEncodingException -> "${throwable.extractExceptionName()}: ${throwable.localizedMessage}"

            is TimeoutException,
            is SocketTimeoutException -> "Timeout: ${throwable.localizedMessage}"

            is SocketException,
            is UnknownServiceException,
            is GeneralSecurityException -> throwable.extractExceptionName()

            is IOException -> "IO: ${throwable.extractExceptionName()}: ${throwable.localizedMessage}"
            else -> null
        }
    }

    private inline fun <reified T : Throwable> T.extractExceptionName(): String? {
        return this::class.simpleName?.removeSuffix("Exception")
    }

    private fun userMessage(exception: HttpException): String? {
        val apiError = apiErrorParser.getCommonError(exception)
        if (apiError != null) {
            return apiError.message
        }
        val validationError = apiErrorParser.getValidationError(exception)
        if (validationError != null) {
            val fieldErrors = validationError.errors.entries.firstOrNull()
            val error = fieldErrors?.value?.firstOrNull()
            if (error != null) {
                return error
            }
        }
        return exception.localizedMessage
    }
}