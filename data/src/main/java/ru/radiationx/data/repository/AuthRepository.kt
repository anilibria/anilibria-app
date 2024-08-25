package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.radiationx.data.apinext.AuthTokenStorage
import ru.radiationx.data.apinext.LogoutCleaner
import ru.radiationx.data.apinext.datasources.AuthApiDataSource
import ru.radiationx.data.apinext.datasources.ProfileApiDataSource
import ru.radiationx.data.apinext.models.AuthToken
import ru.radiationx.data.apinext.models.Credentials
import ru.radiationx.data.apinext.models.LoginSocial
import ru.radiationx.data.apinext.models.OtpCode
import ru.radiationx.data.apinext.models.SocialState
import ru.radiationx.data.apinext.models.SocialType
import ru.radiationx.data.apinext.models.User
import ru.radiationx.data.datasource.holders.AuthHolder
import ru.radiationx.data.datasource.holders.UserHolder
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.entity.domain.auth.OtpInfo
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class AuthRepository @Inject constructor(
    private val authApi: AuthApiDataSource,
    private val userHolder: UserHolder,
    private val authHolder: AuthHolder,
    private val tokenStorage: AuthTokenStorage,
    private val logoutCleaner: LogoutCleaner,
    private val profileApi: ProfileApiDataSource
) {

    fun observeUser(): Flow<User?> =
        combine(observeAuthState(), userHolder.observeUser()) { authState, profileItem ->
            profileItem?.takeIf { authState == AuthState.AUTH }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)


    suspend fun getUser(): User? {
        return withContext(Dispatchers.IO) {
            userHolder.getUser()?.takeIf {
                getAuthState() == AuthState.AUTH
            }
        }
    }

    fun observeAuthState(): Flow<AuthState> = combine(
        tokenStorage.observe(),
        authHolder.observeAuthSkipped()
    ) { cookies, skipped ->
        computeAuthState(cookies, skipped)
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    suspend fun getAuthState(): AuthState {
        return withContext(Dispatchers.IO) {
            computeAuthState(tokenStorage.get(), authHolder.getAuthSkipped())
        }
    }

    suspend fun setAuthSkipped(value: Boolean) {
        withContext(Dispatchers.IO) {
            authHolder.setAuthSkipped(value)
        }
    }

    suspend fun loadUser(): User = withContext(Dispatchers.IO) {
        val user = profileApi.getProfile().user
        userHolder.saveUser(user)
        user
    }

    suspend fun getOtpInfo(): OtpInfo = withContext(Dispatchers.IO) {
        authApi.getOtp(authHolder.getDeviceId())
    }

    suspend fun acceptOtp(code: OtpCode) = withContext(Dispatchers.IO) {
        authApi.acceptOtp(code)
    }

    suspend fun signInOtp(code: OtpCode): User = withContext(Dispatchers.IO) {
        val token = authApi.loginOtp(code, authHolder.getDeviceId())
        handleNewToken(token)
    }

    suspend fun signIn(credentials: Credentials): User = withContext(Dispatchers.IO) {
        val token = authApi.login(credentials)
        handleNewToken(token)
    }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            coRunCatching {
                authApi.logout()
            }.onFailure {
                Timber.e(it)
            }
            logoutCleaner.clear()
        }
    }

    suspend fun loadSocial(type: SocialType): LoginSocial = withContext(Dispatchers.IO) {
        authApi.loginSocial(type)
    }

    suspend fun signInSocial(
        resultUrl: String,
        socialState: SocialState
    ): User = withContext(Dispatchers.IO) {
        authApi.callbackSocial(resultUrl)
        val token = authApi.authenticateSocial(socialState)
        handleNewToken(token)
    }

    private suspend fun handleNewToken(token: AuthToken): User {
        tokenStorage.save(token)
        return loadUser()
    }

    private fun computeAuthState(token: AuthToken?, skipped: Boolean): AuthState {
        return when {
            token != null -> AuthState.AUTH
            skipped -> AuthState.AUTH_SKIPPED
            else -> AuthState.NO_AUTH
        }
    }

}