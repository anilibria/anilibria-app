package ru.radiationx.data.datasource.remote.api

import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.ApiError
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchResult
import ru.radiationx.data.datasource.remote.parsers.AuthParser
import ru.radiationx.data.entity.app.auth.OtpInfo
import ru.radiationx.data.entity.app.auth.SocialAuth
import ru.radiationx.data.entity.app.auth.SocialAuthException
import ru.radiationx.data.entity.app.other.ProfileItem
import ru.radiationx.shared.ktx.android.nullString
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class AuthApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val authParser: AuthParser,
    private val apiConfig: ApiConfig
) {

    suspend fun loadUser(): ProfileItem {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "user"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchResult<JSONObject>()
            .let { authParser.parseUser(it) }
    }

    suspend fun loadOtpInfo(deviceId: String): OtpInfo {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "auth_get_otp",
            "deviceId" to deviceId
        )
        return try {
            client
                .post(apiConfig.apiUrl, args)
                .fetchResult<JSONObject>()
                .let { authParser.parseOtp(it) }
        } catch (ex: Throwable) {
            throw authParser.checkOtpError(ex)
        }
    }

    suspend fun acceptOtp(code: String) {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "auth_accept_otp",
            "code" to code
        )
        try {
            client
                .post(apiConfig.apiUrl, args)
                .fetchResult<JSONObject>()
        } catch (ex: Throwable) {
            throw authParser.checkOtpError(ex)
        }
    }

    suspend fun signInOtp(code: String, deviceId: String): ProfileItem {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "auth_login_otp",
            "deviceId" to deviceId,
            "code" to code
        )
        return try {
            client
                .post(apiConfig.apiUrl, args)
                .fetchResult<JSONObject>()
                .let { loadUser() }
        } catch (ex: Throwable) {
            throw authParser.checkOtpError(ex)
        }
    }

    suspend fun signIn(login: String, password: String, code2fa: String): ProfileItem {
        val args: MutableMap<String, String> = mutableMapOf(
            "mail" to login,
            "passwd" to password,
            "fa2code" to code2fa
        )
        val url = "${apiConfig.baseUrl}/public/login.php"
        return client.post(url, args)
            .let { authParser.authResult(it) }
            .let { loadUser() }
    }

    suspend fun loadSocialAuth(): List<SocialAuth> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "social_auth"
        )
        return client
            .post(apiConfig.apiUrl, args)
            .fetchResult<JSONArray>()
            .let { authParser.parseSocialAuth(it) }
    }

    suspend fun signInSocial(resultUrl: String, item: SocialAuth): ProfileItem {
        val args: MutableMap<String, String> = mutableMapOf()

        val fixedUrl = Uri.parse(apiConfig.baseUrl).host?.let { redirectDomain ->
            resultUrl.replace("www.anilibria.tv", redirectDomain)
        } ?: resultUrl

        return client
            .getFull(fixedUrl, args)
            .also { response ->
                val matcher = Pattern.compile(item.errorUrlPattern).matcher(response.redirect)
                if (matcher.find()) {
                    throw SocialAuthException()
                }
            }
            .also {
                val message = try {
                    JSONObject(it.body).nullString("mes")
                } catch (ignore: Exception) {
                    null
                }
                if (message != null) {
                    throw ApiError(400, message, null)
                }
            }
            .let { loadUser() }
    }

    suspend fun signOut(): String {
        val args = mapOf<String, String>()
        return client.post("${apiConfig.baseUrl}/public/logout.php", args)
    }

}