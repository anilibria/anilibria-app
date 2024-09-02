package ru.radiationx.data.apinext.datasources

import anilibria.api.auth.AuthApi
import anilibria.api.auth.models.LoginRequest
import anilibria.api.auth.models.OtpAcceptRequest
import anilibria.api.auth.models.OtpGetRequest
import anilibria.api.auth.models.OtpLoginRequest
import anilibria.api.auth.models.PasswordForgetRequest
import anilibria.api.auth.models.PasswordResetRequest
import ru.radiationx.data.apinext.models.AuthToken
import ru.radiationx.data.apinext.models.LoginSocial
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.entity.domain.auth.OtpInfo
import toothpick.InjectConstructor

@InjectConstructor
class AuthApiDataSource(
    private val api: AuthApi
) {

    suspend fun getOtp(deviceId: String): OtpInfo {
        return api.getOtp(OtpGetRequest(deviceId)).toDomain()
    }

    suspend fun acceptOtp(code: String) {
        return api.acceptOtp(OtpAcceptRequest(code))
    }

    suspend fun loginOtp(code: String, deviceId: String): AuthToken {
        return api.loginOtp(OtpLoginRequest(code, deviceId)).toDomain()
    }


    suspend fun login(login: String, password: String): AuthToken {
        return api.login(LoginRequest(login, password)).toDomain()
    }

    suspend fun logout() {
        api.logout()
    }


    suspend fun loginSocial(provider: String): LoginSocial {
        return api.loginSocial(provider).toDomain()
    }

    suspend fun authenticateSocial(state: String): AuthToken {
        return api.authenticateSocial(state).toDomain()
    }

    suspend fun passwordForget(email: String) {
        return api.passwordForget(PasswordForgetRequest(email))
    }

    suspend fun passwordReset(token: String, password: String, passwordConfirmation: String) {
        return api.passwordReset(PasswordResetRequest(token, password, passwordConfirmation))
    }
}