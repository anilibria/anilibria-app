package ru.radiationx.anilibria.model.repository

import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.entity.app.auth.SocialAuth
import ru.radiationx.data.entity.app.other.ProfileItem
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.anilibria.model.data.holders.SocialAuthHolder
import ru.radiationx.anilibria.model.data.holders.UserHolder
import ru.radiationx.anilibria.model.data.remote.ApiError
import ru.radiationx.anilibria.model.data.remote.api.AuthApi
import ru.radiationx.anilibria.model.system.HttpException
import ru.radiationx.anilibria.model.system.SchedulersProvider
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class AuthRepository @Inject constructor(
        private val schedulers: SchedulersProvider,
        private val authApi: AuthApi,
        private val userHolder: UserHolder,
        private val socialAuthHolder: SocialAuthHolder
) {

    /*private val socialAuthInfo = listOf(SocialAuth(
            "vk",
            "ВКонтакте",
            "https://oauth.vk.com/authorize?client_id=5315207&redirect_uri=https://www.anilibria.tv/public/vk.php",
            "https?:\\/\\/(?:(?:www|api)?\\.)?anilibria\\.tv\\/public\\/vk\\.php([?&]code)",
            "https?:\\/\\/(?:(?:www|api)?\\.)?anilibria\\.tv\\/pages\\/vk\\.php"
    ))*/

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

    private fun updateUser(newUser: ProfileItem) {
        newUser.authState = AuthState.AUTH
        userHolder.saveUser(newUser)
    }

    // охеренный метод, которым проверяем авторизацию и одновременно подтягиваем юзера. двойной профит.
    fun loadUser(): Single<ProfileItem> = authApi
            .loadUser()
            .doOnSuccess { updateUser(it) }
            .doOnError {
                it.printStackTrace()
                val code = ((it as? ApiError)?.code ?: (it as? HttpException)?.code)
                if (code == 401) {
                    userHolder.delete()
                }
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun signIn(login: String, password: String, code2fa: String): Single<ProfileItem> = authApi
            .signIn(login, password, code2fa)
            .doOnSuccess { userHolder.saveUser(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun signOut(): Single<String> = authApi
            .signOut()
            .doOnSuccess {
                userHolder.delete()
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun observeSocialAuth(): Observable<List<SocialAuth>> = socialAuthHolder
            .observe()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun loadSocialAuth(): Single<List<SocialAuth>> = authApi
            .loadSocialAuth()
            .doOnSuccess { socialAuthHolder.save(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getSocialAuth(key: String): Single<SocialAuth> = Single
            .just(socialAuthHolder.get().first { it.key == key })
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun signInSocial(resultUrl: String, item: SocialAuth): Single<ProfileItem> = authApi
            .signInSocial(resultUrl, item)
            .doOnSuccess { userHolder.saveUser(it) }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}