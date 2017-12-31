package ru.radiationx.anilibria.model.repository

import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.holders.AuthHolder
import ru.radiationx.anilibria.model.data.holders.CookieHolder
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.api.AuthApi
import ru.radiationx.anilibria.model.system.ApiUtils
import ru.radiationx.anilibria.model.system.SchedulersProvider
import java.util.regex.Pattern

/**
 * Created by radiationx on 30.12.17.
 */
class AuthRepository constructor(private val schedulers: SchedulersProvider,
                                 private val apiUtils: IApiUtils,
                                 private val authApi: AuthApi,
                                 private val authHolder: AuthHolder,
                                 private val cookieHolder: CookieHolder) {

    //val socialPattern = "<div[^>]*?id=\"bx_auth_serv_formPatreon\"[^>]*?>[^<]*?<a[^>]*?href=\"([^\"]*?)\"[^>]*?>|<div[^>]*?id=\"bx_auth_serv_formVKontakte\"[^>]*?>[^<]*?<a[^>]*?onclick=\"BX.util.popup\\(['\"]([^\"']*?)['\"]"
    val patreonPattern = "<div[^>]*?id=\"bx_auth_serv_formPatreon\"[^>]*?>[^<]*?<a[^>]*?href=\"([^\"]*?)\"[^>]*?>"
    val vkPattern = "<div[^>]*?id=\"bx_auth_serv_formVKontakte\"[^>]*?>[^<]*?<a[^>]*?onclick=\"BX.util.popup\\(['\"]([^\"']*?)['\"]"

    val socialPatterns = arrayOf(patreonPattern, vkPattern)
    val userPattern = "<div[^>]*?class=\"[^\"]*?userinfo[^\"]*?\"[^>]*?>[^<]*?<p[^>]*?>([\\s\\S]*?)<\\/p>"
    val oldCheckPattern = "<div[^>]*?class=\"[^\"]*?user-auth-main-block[^\"]*?\"[^>]*?>[\\s\\S]*?<form[^>]*?action=\"\\/auth[^\"]*?\">"

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
            .doOnSuccess {
                var user: String? = null
                val matcher = Pattern.compile(userPattern).matcher(it)
                if (matcher.find()) {
                    user = matcher.group(1)
                }
                val token = cookieHolder.getCookies()[CookieHolder.BITRIX_SM_UIDH]
                val isAuth = user != null && token != null
                val state = if (isAuth) AuthState.AUTH else AuthState.NO_AUTH
                setAuthState(state)
                Log.e("SUKA", "User name: " + user)
            }
            .map { authState.value }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun signIn(redirectUrl: String): Single<AuthState>
            = authApi.patreonAuth(redirectUrl+stateSite)
            .doOnSuccess {
                var user: String? = null
                val matcher = Pattern.compile(userPattern).matcher(it)
                if (matcher.find()) {
                    user = matcher.group(1)
                }
                val isAuth = user != null
                val state = if (isAuth) AuthState.AUTH else AuthState.NO_AUTH
                setAuthState(state)
                Log.e("SUKA", "User name: " + user)
            }
            .map { authState.value }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun loadAuthPage(): Single<MutableList<String>>
            = authApi.loadAuthPage()
            .map {
                var user: String? = null
                val matcher1 = Pattern.compile(userPattern).matcher(it)
                if (matcher1.find()) {
                    user = matcher1.group(1)
                }
                Log.e("SUKA", "User name in load auth: " + user)
                val result = mutableListOf<String>()
                socialPatterns.forEach { pattern ->
                    val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(it)
                    if (matcher.find()) {
                        result.add(matcher.group(1).replace("&amp;".toRegex(), "&"))
                    }
                }
                result.forEachIndexed { index, s ->
                    val matcher = Pattern.compile("&state[\\s\\S]*").matcher(s)
                    if (matcher.find()) {
                        stateSite = matcher.group(0)
                        result[index] = s.replace(stateSite, "")
                    }
                }
                result
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}