package ru.radiationx.data.repository

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.datasource.holders.AuthHolder
import ru.radiationx.data.datasource.holders.SocialAuthHolder
import ru.radiationx.data.datasource.holders.UserHolder
import ru.radiationx.data.datasource.remote.ApiError
import ru.radiationx.data.datasource.remote.api.AuthApi
import ru.radiationx.data.entity.app.auth.OtpInfo
import ru.radiationx.data.entity.app.auth.SocialAuth
import ru.radiationx.data.entity.app.other.ProfileItem
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.system.HttpException
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userHolder: UserHolder,
    private val authHolder: AuthHolder,
    private val socialAuthHolder: SocialAuthHolder
) {

    /*private val socialAuthInfo = listOf(SocialAuth(
            "vk",
            "ВКонтакте",
            "https://oauth.vk.com/authorize?client_id=5315207&redirect_uri=https://www.anilibria.tv/public/vk.php",
            "https?:\\/\\/(?:(?:www|api)?\\.)?anilibria\\.tv\\/public\\/vk\\.php([?&]code)",
            "https?:\\/\\/(?:(?:www|api)?\\.)?anilibria\\.tv\\/pages\\/vk\\.php"
    ))*/

    fun observeUser(): Flow<ProfileItem> = userHolder.observeUser()

    fun getUser() = userHolder.getUser()

    fun getAuthState(): AuthState = userHolder.getUser().authState

    fun updateUser(authState: AuthState) {
        val user = userHolder.getUser().copy(
            authState = authState
        )
        userHolder.saveUser(user)
    }

    private fun updateUser(newUser: ProfileItem) {
        val user = newUser.copy(
            authState = AuthState.AUTH
        )
        userHolder.saveUser(user)
    }

    // охеренный метод, которым проверяем авторизацию и одновременно подтягиваем юзера. двойной профит.
    suspend fun loadUser(): ProfileItem {
        return try {
            authApi
                .loadUser()
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

    suspend fun getOtpInfo(): OtpInfo = authApi.loadOtpInfo(authHolder.getDeviceId())

    suspend fun acceptOtp(code: String) = authApi.acceptOtp(code)

    suspend fun signInOtp(code: String): ProfileItem = authApi
        .signInOtp(code, authHolder.getDeviceId())
        .also { userHolder.saveUser(it) }

    suspend fun signIn(login: String, password: String, code2fa: String): ProfileItem =
        authApi
            .signIn(login, password, code2fa)
            .also { userHolder.saveUser(it) }

    suspend fun signOut(): String = authApi
        .signOut()
        .also {
            userHolder.delete()
        }

    fun observeSocialAuth(): Flow<List<SocialAuth>> = socialAuthHolder.observe()

    suspend fun loadSocialAuth(): List<SocialAuth> = authApi
        .loadSocialAuth()
        .also { socialAuthHolder.save(it) }

    suspend fun getSocialAuth(key: String): SocialAuth =
        socialAuthHolder.get().first { it.key == key }

    suspend fun signInSocial(resultUrl: String, item: SocialAuth): ProfileItem = authApi
        .signInSocial(resultUrl, item)
        .also { userHolder.saveUser(it) }

}