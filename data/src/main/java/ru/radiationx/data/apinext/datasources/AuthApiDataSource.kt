package ru.radiationx.data.apinext.datasources

import anilibria.api.auth.AuthApi
import anilibria.api.auth.models.LoginRequest
import anilibria.api.auth.models.OtpAcceptRequest
import anilibria.api.auth.models.OtpGetRequest
import anilibria.api.auth.models.OtpLoginRequest
import anilibria.api.auth.models.PasswordForgetRequest
import anilibria.api.auth.models.PasswordResetRequest
import ru.radiationx.data.apinext.models.SocialState
import ru.radiationx.data.apinext.models.AuthToken
import ru.radiationx.data.apinext.models.Credentials
import ru.radiationx.data.apinext.models.DeviceId
import ru.radiationx.data.apinext.models.LoginSocial
import ru.radiationx.data.apinext.models.OtpCode
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.entity.domain.auth.OtpInfo
import toothpick.InjectConstructor

@InjectConstructor
class AuthApiDataSource(
    private val api: AuthApi
) {

    suspend fun getOtp(deviceId: DeviceId): OtpInfo {
        return api.getOtp(OtpGetRequest(deviceId.id)).toDomain()
    }

    suspend fun acceptOtp(code: OtpCode) {
        return api.acceptOtp(OtpAcceptRequest(code.code))
    }

    suspend fun loginOtp(code: OtpCode, deviceId: DeviceId): AuthToken {
        return api.loginOtp(OtpLoginRequest(code.code, deviceId.id)).toDomain()
    }


    suspend fun login(credentials: Credentials): AuthToken {
        return api.login(LoginRequest(credentials.login, credentials.password)).toDomain()
    }

    suspend fun logout() {
        api.logout()
    }


    suspend fun loginSocial(provider: String): LoginSocial {
        return api.loginSocial(provider).toDomain()
    }

    suspend fun authenticateSocial(state: SocialState): AuthToken {
        return api.authenticateSocial(state.state).toDomain()
    }

    suspend fun passwordForget(email: String) {
        return api.passwordForget(PasswordForgetRequest(email))
    }

    suspend fun passwordReset(token: String, password: String, passwordConfirmation: String) {
        return api.passwordReset(PasswordResetRequest(token, password, passwordConfirmation))
    }
}