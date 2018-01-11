package ru.radiationx.anilibria.model.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.holders.CookieHolder
import ru.radiationx.anilibria.model.data.holders.UserHolder
import ru.radiationx.anilibria.model.data.remote.api.AuthApi
import ru.radiationx.anilibria.model.system.SchedulersProvider
import java.util.regex.Pattern

/**
 * Created by radiationx on 30.12.17.
 */
class AuthRepository constructor(
        private val schedulers: SchedulersProvider,
        private val authApi: AuthApi,
        private val userHolder: UserHolder,
        private val cookieHolder: CookieHolder
) {

    var stateSite = ""

    private val userRelay = BehaviorRelay.createDefault(userHolder.getUser())

    fun observeUser(): Observable<ProfileItem> = userRelay

    fun getUser(): ProfileItem = userRelay.value

    fun getAuthState(): AuthState = getUser().authState

    fun updateUser(user: ProfileItem) {
        userRelay.accept(user)
        userHolder.saveUser(user)
    }

    fun updateUser(authState: AuthState) {
        val user = getUser()
        user.authState = authState
        updateUser(user)
    }

    fun signIn(login: String, password: String): Single<ProfileItem> = authApi
            .testAuth(login, password)
            .doOnSuccess { updateUser(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun signIn(redirectUrl: String): Single<ProfileItem> = authApi
            .socialAuth(redirectUrl + stateSite)
            .doOnSuccess { updateUser(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun loadAuthPage(): Single<List<String>> = authApi
            .loadAuthPage()
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

    fun signOut() {
        val user = getUser()
        user.apply {
            authState = AuthState.NO_AUTH
            id = ProfileItem.NO_ID
            nick = ProfileItem.NO_VALUE
            avatarUrl = ProfileItem.NO_VALUE
        }
        updateUser(user)
        CookieHolder.cookieNames.forEach {
            cookieHolder.removeCookie(it)
        }
    }
}