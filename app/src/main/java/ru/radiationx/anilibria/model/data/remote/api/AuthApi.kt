package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.AuthParser
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class AuthApi @Inject constructor(
        private val client: IClient,
        private val authParser: AuthParser
) {

    fun loadUser(): Single<ProfileItem> {
        val args: MutableMap<String, String> = mutableMapOf(
                "query" to "user"
        )
        return client.post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { authParser.parseUser(it) }
    }

    fun signIn(login: String, password: String, code2fa: String): Single<ProfileItem> {
        val args: MutableMap<String, String> = mutableMapOf(
                "mail" to login,
                "passwd" to password,
                "fa2code" to code2fa
        )
        val url = "${Api.BASE_URL}/public/login.php"
        return client.post(url, args)
                .map { authParser.authResult(it) }
                .flatMap { loadUser() }
    }

    fun signOut(): Single<String> {
        val args = mapOf<String, String>()
        return client.post("${Api.BASE_URL}/public/logout.php", args)
    }

}