package ru.radiationx.anilibria.model.repository

import io.reactivex.Observable
import io.reactivex.Single
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

    private var stateSite = ""

    fun observeUser(): Observable<ProfileItem> = userHolder
            .observeUser()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getUser() = userHolder.getUser()

    fun getAuthState(): AuthState = userHolder.getUser().authState

    fun updateUser(authState: AuthState) {
        val user = userHolder.getUser()
        user.authState = authState
        userHolder.saveUser(user)
    }

    fun signIn(login: String, password: String): Single<ProfileItem> = authApi
            .testAuth(login, password)
            .doOnSuccess {
                userHolder.saveUser(it)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun signIn(redirectUrl: String): Single<ProfileItem> = authApi
            .socialAuth(redirectUrl + stateSite)
            .doOnSuccess {
                userHolder.saveUser(it)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun loadAuthPage(): Single<List<String>> = authApi
            .loadAuthPage()
            .map {
                val newList = mutableListOf<String>()
                it.forEachIndexed { _, s ->
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

    fun signOut(): Single<String> = authApi
            .signOut()
            .doOnSuccess {
               // hardSignOut()
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}