package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient

/**
 * Created by radiationx on 30.12.17.
 */
class AuthApi(private val client: IClient,
              apiUtils: IApiUtils) {


    fun loadAuthPage(): Single<String> {
        val args: MutableMap<String, String> = mutableMapOf()
        val url = "${Api.BASE_URL}auth"
        return client.get(url, args)/*.map { articleParser.article(it) }*/
    }

    fun patreonAuth(redirectUrl: String): Single<String> {
        return client.get(redirectUrl, emptyMap())
                .map { loadAuthPage().blockingGet() }
                //.map { client.get(redirectUrl, emptyMap()).blockingGet() }
    }

    fun testAuth(login: String, password: String): Single<String> {
        val args: MutableMap<String, String> = mutableMapOf(
                "backurl" to "/auth/",
                "AUTH_FORM" to "Y",
                "TYPE" to "AUTH",
                "USER_REMEMBER" to "Y",
                "USER_LOGIN" to login,
                "USER_PASSWORD" to password
        )
        val url = "${Api.BASE_URL}auth/?login=yes"
        return client.post(url, args)/*.map { articleParser.article(it) }*/
    }

}