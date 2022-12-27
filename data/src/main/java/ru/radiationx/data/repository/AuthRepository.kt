package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import ru.radiationx.data.datasource.holders.AuthHolder
import ru.radiationx.data.datasource.holders.CookieHolder
import ru.radiationx.data.datasource.holders.SocialAuthHolder
import ru.radiationx.data.datasource.holders.UserHolder
import ru.radiationx.data.datasource.remote.ApiError
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.AuthApi
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.entity.domain.auth.OtpInfo
import ru.radiationx.data.entity.domain.auth.SocialAuth
import ru.radiationx.data.entity.domain.other.ProfileItem
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.system.HttpException
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userHolder: UserHolder,
    private val authHolder: AuthHolder,
    private val socialAuthHolder: SocialAuthHolder,
    private val apiConfig: ApiConfig,
    private val cookieHolder: CookieHolder
) {

    fun observeUser(): Flow<ProfileItem?> =
        combine(observeAuthState(), userHolder.observeUser()) { authState, profileItem ->
            profileItem?.takeIf { authState == AuthState.AUTH }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)


    suspend fun getUser(): ProfileItem? {
        return withContext(Dispatchers.IO) {
            userHolder.getUser()?.takeIf {
                getAuthState() == AuthState.AUTH
            }
        }
    }

    fun observeAuthState(): Flow<AuthState> = combine(
        cookieHolder.observeCookies(),
        authHolder.observeAuthSkipped()
    ) { cookies, skipped ->
        computeAuthState(cookies, skipped)
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    suspend fun getAuthState(): AuthState {
        return withContext(Dispatchers.IO) {
            computeAuthState(cookieHolder.getCookies(), authHolder.getAuthSkipped())
        }
    }

    suspend fun setAuthSkipped(value: Boolean) {
        withContext(Dispatchers.IO) {
            authHolder.setAuthSkipped(value)
        }
    }

    // охеренный метод, которым проверяем авторизацию и одновременно подтягиваем юзера. двойной профит.
    suspend fun loadUser(): ProfileItem {
        return withContext(Dispatchers.IO) {
            try {
                authApi
                    .loadUser()
                    .toDomain(apiConfig)
                    .also { updateUser(it) }
            } catch (ex: Throwable) {
                Timber.e(ex)
                val code = ((ex as? ApiError)?.code ?: (ex as? HttpException)?.code)
                if (code == 401) {
                    userHolder.delete()
                }
                throw ex
            }
        }
    }

    suspend fun getOtpInfo(): OtpInfo = withContext(Dispatchers.IO) {
        authApi
            .loadOtpInfo(authHolder.getDeviceId())
            .toDomain()
    }

    suspend fun acceptOtp(code: String) = withContext(Dispatchers.IO) {
        authApi.acceptOtp(code)
    }

    suspend fun signInOtp(code: String): ProfileItem = withContext(Dispatchers.IO) {
        authApi
            .signInOtp(code, authHolder.getDeviceId())
            .toDomain(apiConfig)
            .also { updateUser(it) }
    }

    suspend fun signIn(login: String, password: String, code2fa: String): ProfileItem =
        withContext(Dispatchers.IO) {
            authApi
                .signIn(login, password, code2fa)
                .toDomain(apiConfig)
                .also { updateUser(it) }
        }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            coRunCatching {
                authApi.signOut()
            }.onFailure {
                Timber.e(it)
            }
            cookieHolder.removeCookie(CookieHolder.PHPSESSID)
            userHolder.delete()
        }
    }

    fun observeSocialAuth(): Flow<List<SocialAuth>> = socialAuthHolder
        .observe()
        .flowOn(Dispatchers.IO)

    suspend fun loadSocialAuth(): List<SocialAuth> = withContext(Dispatchers.IO) {
        authApi
            .loadSocialAuth()
            .map { it.toDomain() }
            .also { socialAuthHolder.save(it) }
    }

    suspend fun getSocialAuth(key: String): SocialAuth =
        withContext(Dispatchers.IO) {
            socialAuthHolder.get().first { it.key == key }
        }

    suspend fun signInSocial(resultUrl: String, item: SocialAuth): ProfileItem =
        withContext(Dispatchers.IO) {
            authApi
                .signInSocial(resultUrl, item)
                .toDomain(apiConfig)
                .also { updateUser(it) }
        }

    private suspend fun updateUser(newUser: ProfileItem) {
        withContext(Dispatchers.IO) {
            userHolder.saveUser(newUser)
        }
    }

    private fun computeAuthState(cookies: Map<String, Cookie>, skipped: Boolean): AuthState {
        val cookie = cookies[CookieHolder.PHPSESSID]
        return when {
            cookie != null -> AuthState.AUTH
            skipped -> AuthState.AUTH_SKIPPED
            else -> AuthState.NO_AUTH
        }
    }

}