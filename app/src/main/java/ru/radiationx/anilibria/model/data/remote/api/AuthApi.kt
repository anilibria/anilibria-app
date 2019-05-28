package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.auth.SocialAuth
import ru.radiationx.anilibria.entity.app.auth.SocialAuthException
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.extension.nullString
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiError
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.data.remote.parsers.AuthParser
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class AuthApi @Inject constructor(
        private val client: IClient,
        private val authParser: AuthParser,
        private val apiConfig: ApiConfig
) {

    fun loadUser(): Single<ProfileItem> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "user"
        )
        return client.post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { authParser.parseUser(it) }
    }

    fun signIn(login: String, password: String, code2fa: String): Single<ProfileItem> {
        val args: MutableMap<String, String> = mutableMapOf(
                "mail" to login,
                "passwd" to password,
                "fa2code" to code2fa
        )
        val url = "${apiConfig.baseUrl}/public/login.php"
        return client.post(url, args)
                .map { authParser.authResult(it) }
                .flatMap { loadUser() }
    }

    fun loadSocialAuth(): Single<List<SocialAuth>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "social_auth"
        )
        return client
                .post(apiConfig.apiUrl, args)
                .compose(ApiResponse.fetchResult<JSONArray>())
                .map { authParser.parseSocialAuth(it) }
    }

    fun signInSocial(resultUrl: String, item: SocialAuth): Single<ProfileItem> {
        val args: MutableMap<String, String> = mutableMapOf()
        return client
                .getFull(resultUrl, args)
                .doOnSuccess { response ->
                    val matcher = Pattern.compile(item.errorUrlPattern).matcher(response.redirect)
                    if (matcher.find()) {
                        throw SocialAuthException()
                    }
                }
                .doOnSuccess {
                    val message = try {
                        JSONObject(it.body).nullString("mes")
                    } catch (ignore: Exception) {
                        null
                    }
                    if (message != null) {
                        throw ApiError(400, message, null)
                    }
                }
                .flatMap { loadUser() }
    }

    fun signOut(): Single<String> {
        val args = mapOf<String, String>()
        return client.post("${apiConfig.baseUrl}/public/logout.php", args)
    }

}