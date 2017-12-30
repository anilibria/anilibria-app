package ru.radiationx.anilibria.model.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.holders.AuthHolder
import ru.radiationx.anilibria.model.data.holders.CookieHolder
import ru.radiationx.anilibria.model.data.remote.api.AuthApi
import ru.radiationx.anilibria.model.system.SchedulersProvider
import java.util.regex.Pattern

/**
 * Created by radiationx on 30.12.17.
 */
class AuthRepository constructor(private val schedulers: SchedulersProvider,
                                 private val authApi: AuthApi,
                                 private val authHolder: AuthHolder,
                                 private val cookieHolder: CookieHolder) {

    private val authState = BehaviorRelay.createDefault(authHolder.getAuthState())

    fun observeAuthState(): Observable<AuthState> = authState

    fun getAuthState(): AuthState = authState.value

    fun setAuthState(state: AuthState) {
        authHolder.setAuthState(state)
        authState.accept(state)
    }

    fun signIn(login: String, password: String): Single<AuthState>
            = authApi.testAuth(login, password)
            .doOnSuccess {
                val pattern = Pattern.compile("<div[^>]*?class=\"[^\"]*?user-auth-main-block[^\"]*?\"[^>]*?>[\\s\\S]*?<form[^>]*?action=\"\\/auth[^\"]*?\">", Pattern.CASE_INSENSITIVE)
                val token = cookieHolder.getCookies()[CookieHolder.BITRIX_SM_UIDH]
                val isAuth = !pattern.matcher(it).find() && token != null
                val state = if (isAuth) AuthState.AUTH else AuthState.NO_AUTH
                setAuthState(state)
            }
            .map { authState.value }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}