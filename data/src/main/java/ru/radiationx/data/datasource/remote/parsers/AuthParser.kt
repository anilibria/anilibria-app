package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONObject
import ru.radiationx.data.datasource.remote.ApiError
import ru.radiationx.data.entity.domain.auth.EmptyFieldException
import ru.radiationx.data.entity.domain.auth.InvalidUserException
import ru.radiationx.data.entity.domain.auth.OtpAcceptedException
import ru.radiationx.data.entity.domain.auth.OtpNotAcceptedException
import ru.radiationx.data.entity.domain.auth.OtpNotFoundException
import ru.radiationx.data.entity.domain.auth.Wrong2FaCodeException
import ru.radiationx.data.entity.domain.auth.WrongPasswordException
import ru.radiationx.data.entity.domain.auth.WrongUserAgentException
import ru.radiationx.shared.ktx.android.nullString
import javax.inject.Inject

/**
 * Created by radiationx on 31.12.17.
 */
class AuthParser @Inject constructor() {

    fun checkOtpError(error: Throwable): Throwable = if (error is ApiError) {
        when (error.description) {
            "otpNotFound" -> OtpNotFoundException(error.message.orEmpty())
            "otpAccepted" -> OtpAcceptedException(error.message.orEmpty())
            "otpNotAccepted" -> OtpNotAcceptedException(error.message.orEmpty())
            else -> error
        }
    } else {
        error
    }

    fun authResult(responseText: String): String {
        val responseJson = JSONObject(responseText)
        val error = responseJson.nullString("err")
        val message = responseJson.nullString("mes")
        val key = responseJson.nullString("key")
        if (error != "ok" && key != "authorized") {
            val apiError = ApiError(400, message ?: key, null)
            throw when (key) {
                //ignore
                //"authorized" -> AlreadyAuthorizedException(apiError)
                "empty" -> EmptyFieldException(apiError)
                "wrongUserAgent" -> WrongUserAgentException(apiError)
                "invalidUser" -> InvalidUserException(apiError)
                "wrong2FA" -> Wrong2FaCodeException(apiError)
                "wrongPasswd" -> WrongPasswordException(apiError)
                else -> apiError
            }
        }
        return message.orEmpty()
    }

}