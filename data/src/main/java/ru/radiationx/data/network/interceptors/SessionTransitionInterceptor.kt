package ru.radiationx.data.network.interceptors

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Cookie
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.HttpException
import ru.radiationx.data.api.auth.AuthTokenStorage
import ru.radiationx.data.api.auth.legacy.CookieHolder
import ru.radiationx.data.api.auth.mapper.toDomain
import ru.radiationx.data.app.DirectApi
import ru.radiationx.data.app.config.AppConfig
import ru.radiationx.data.common.toPathUrl
import ru.radiationx.shared.ktx.coRunCatching
import javax.inject.Inject

class SessionTransitionInterceptor @Inject constructor(
    private val cookieHolder: CookieHolder,
    private val tokenStorage: AuthTokenStorage,
    private val directApi: DirectApi,
    private val appConfig: AppConfig
) : SuspendableInterceptor() {

    companion object {
        private val path = "/api/v1/app/transition/session".toPathUrl()
    }

    private val mutex = Mutex()

    override suspend fun interceptSuspend(chain: Interceptor.Chain): Response {
        if (getInvalidatedAuthCookie() != null) {
            migrateSession()
        }
        return chain.proceed(chain.request())
    }

    private suspend fun getInvalidatedAuthCookie(): Cookie? {
        val authCookie = cookieHolder.getAuthCookie() ?: return null
        val token = tokenStorage.get()
        if (token != null) {
            cookieHolder.removeAuthCookie()
            return null
        }
        return authCookie
    }

    private suspend fun migrateSession() {
        mutex.withLock {
            val authCookie = getInvalidatedAuthCookie() ?: return@withLock
            val requestUrl = appConfig.api.withPath(path)
            val sessionId = authCookie.value
            coRunCatching {
                directApi.transitionSession(requestUrl, sessionId)
            }.onSuccess { token ->
                tokenStorage.save(token.toDomain())
                cookieHolder.removeAuthCookie()
            }.onFailure {
                if (it is HttpException && (it.code() == 422 || it.code() == 404)) {
                    cookieHolder.removeAuthCookie()
                }
            }
        }
    }
}