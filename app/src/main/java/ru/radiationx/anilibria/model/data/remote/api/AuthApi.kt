package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.AuthParser

/**
 * Created by radiationx on 30.12.17.
 */
class AuthApi(
        private val client: IClient,
        private val authParser: AuthParser
) {

    fun loadAuthPage(): Single<List<String>> {
        val args: MutableMap<String, String> = mutableMapOf()
        val url = "${Api.BASE_URL}/auth"
        return client.get(url, args)
                .map { authParser.getSocialLinks(it) }
    }

    fun socialAuth(redirectUrl: String): Single<ProfileItem> {
        val args: MutableMap<String, String> = mutableMapOf(
                "USER_REMEMBER" to "Y"
        )
        return client.post(redirectUrl, args)
                .map { client.get("${Api.BASE_URL}/auth", emptyMap()).blockingGet() }
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { authParser.parseUser(it) }
    }

    fun testAuth(login: String, password: String): Single<ProfileItem> {
        val args: MutableMap<String, String> = mutableMapOf(
                "mail" to login,
                "passwd" to password
        )
        val url = "${Api.BASE_URL}/public/login.php"
        return client.post(url, args)
                .map { authParser.authResult(it) }
                .flatMap { getUser() }
    }

    fun getUser(): Single<ProfileItem> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "user"
        )
        return client.post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { authParser.parseUser(it) }
    }

    fun signOut(): Single<String> {
        val args = mapOf<String, String>()
        return client.post("${Api.BASE_URL}/public/logout.php", args)
    }

}