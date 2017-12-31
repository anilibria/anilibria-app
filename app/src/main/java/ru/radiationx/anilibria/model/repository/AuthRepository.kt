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

    var stateSite = ""

    private val authState = BehaviorRelay.createDefault(authHolder.getAuthState())

    fun observeAuthState(): Observable<AuthState> = authState

    fun getAuthState(): AuthState = authState.value

    fun setAuthState(state: AuthState) {
        authHolder.setAuthState(state)
        authState.accept(state)
    }

    fun signIn(login: String, password: String): Single<AuthState>
            = authApi.testAuth(login, password)
            .doOnSuccess { setAuthState(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun signIn(redirectUrl: String): Single<AuthState>
            = authApi.socialAuth(redirectUrl + stateSite)
            .doOnSuccess { setAuthState(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun loadAuthPage(): Single<List<String>>
            = authApi.loadAuthPage()
            .map {
                val newList = mutableListOf<String>()
                it.forEachIndexed { index, s ->
                    val matcher = Pattern.compile("&state[\\s\\S]*").matcher(s)
                    if (matcher.find()) {
                        stateSite = matcher.group(0)
                        newList.add(s.replace(stateSite, ""))
                    }
                }
                newList as List<String>
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}