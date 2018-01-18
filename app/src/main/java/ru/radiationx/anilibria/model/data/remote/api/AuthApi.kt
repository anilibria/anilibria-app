package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.AuthParser

/**
 * Created by radiationx on 30.12.17.
 */
class AuthApi(private val client: IClient,
              apiUtils: IApiUtils) {

    private val authParser = AuthParser(apiUtils)

    fun loadAuthPage(): Single<List<String>> {
        val args: MutableMap<String, String> = mutableMapOf()
        val url = "${Api.BASE_URL}auth"
        return client.get(url, args)
                .map { authParser.getSocialLinks(it) }
    }

    fun socialAuth(redirectUrl: String): Single<ProfileItem> {
        val args: MutableMap<String, String> = mutableMapOf(
                "backurl" to "/auth/",
                "AUTH_FORM" to "Y",
                "TYPE" to "AUTH",
                "USER_REMEMBER" to "Y"
        )
        return client.post(redirectUrl, args)
                .map { client.get("${Api.BASE_URL}auth", emptyMap()).blockingGet() }
                .map { authParser.authResult(it) }
    }

    fun testAuth(login: String, password: String): Single<ProfileItem> {
        val args: MutableMap<String, String> = mutableMapOf(
                "backurl" to "/auth/",
                "AUTH_FORM" to "Y",
                "TYPE" to "AUTH",
                "USER_REMEMBER" to "Y",
                "USER_LOGIN" to login,
                "USER_PASSWORD" to password
        )
        val url = "${Api.BASE_URL}auth/?login=yes"
        return client.post(url, args)
                .map { authParser.authResult(it) }
    }

}