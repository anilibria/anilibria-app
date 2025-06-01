package ru.radiationx.data.api.auth

import anilibria.api.auth.AuthApi
import anilibria.api.auth.models.LoginRequest
import anilibria.api.auth.models.OtpAcceptRequest
import anilibria.api.auth.models.OtpGetRequest
import anilibria.api.auth.models.OtpLoginRequest
import anilibria.api.auth.models.PasswordForgetRequest
import anilibria.api.auth.models.PasswordResetRequest
import retrofit2.HttpException
import ru.radiationx.data.api.auth.mapper.toDomain
import ru.radiationx.data.api.auth.models.AuthToken
import ru.radiationx.data.api.auth.models.Credentials
import ru.radiationx.data.api.auth.models.DeviceId
import ru.radiationx.data.api.auth.models.LoginSocial
import ru.radiationx.data.api.auth.models.OtpCode
import ru.radiationx.data.api.auth.models.OtpInfo
import ru.radiationx.data.api.auth.models.OtpNotCreatedException
import ru.radiationx.data.api.auth.models.OtpNotFoundException
import ru.radiationx.data.api.auth.models.OtpWrongUserException
import ru.radiationx.data.api.auth.models.SocialAuthException
import ru.radiationx.data.api.auth.models.SocialState
import ru.radiationx.data.api.auth.models.SocialType
import toothpick.InjectConstructor

// TODO API2 update exception mapping
@InjectConstructor
class AuthApiDataSource(
    private val api: AuthApi
) {


    suspend fun getOtp(deviceId: DeviceId): OtpInfo {
        return try {
            api.getOtp(OtpGetRequest(deviceId.id)).toDomain()
        } catch (ex: HttpException) {
            throw when (ex.code()) {
                404 -> OtpNotCreatedException("Не удалось создать OTP")
                else -> throw ex
            }
        }
    }

    suspend fun acceptOtp(code: OtpCode) {
        return try {
            api.acceptOtp(OtpAcceptRequest(code.code))
        } catch (ex: HttpException) {
            throw when (ex.code()) {
                404 -> OtpNotFoundException("OTP не найден")
                else -> throw ex
            }
        }
    }

    suspend fun loginOtp(code: OtpCode, deviceId: DeviceId): AuthToken {
        return try {
            api.loginOtp(OtpLoginRequest(code.code, deviceId.id)).toDomain()
        } catch (ex: HttpException) {
            throw when (ex.code()) {
                401 -> OtpWrongUserException("OTP не привязан к пользователю")
                404 -> OtpNotFoundException("OTP не найден")
                else -> throw ex
            }
        }
    }


    suspend fun login(credentials: Credentials): AuthToken {
        return api.login(LoginRequest(credentials.login, credentials.password)).toDomain()
    }

    suspend fun logout() {
        api.logout()
    }


    suspend fun loginSocial(type: SocialType): LoginSocial {
        val provider = when (type) {
            SocialType.VK -> "vk"
            SocialType.GOOGLE -> "google"
            SocialType.PATREON -> "patreon"
            SocialType.DISCORD -> "discord"
        }
        return api.loginSocial(provider).toDomain()
    }

    suspend fun callbackSocial(resultUrl: String) {
        api.callbackSocial(resultUrl)
    }

    suspend fun authenticateSocial(state: SocialState): AuthToken {
        return try {
            api.authenticateSocial(state.state).toDomain()
        } catch (ex: HttpException) {
            if (ex.code() == 404) {
                throw SocialAuthException()
            }
            throw ex
        }
    }

    suspend fun passwordForget(email: String) {
        return api.passwordForget(PasswordForgetRequest(email))
    }

    suspend fun passwordReset(token: String, password: String, passwordConfirmation: String) {
        return api.passwordReset(PasswordResetRequest(token, password, passwordConfirmation))
    }
}