package ru.radiationx.data.datasource.remote.api

import android.net.Uri
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.ApiError
import ru.radiationx.data.datasource.remote.ApiResponse
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.parsers.AuthParser
import ru.radiationx.data.entity.app.auth.OtpInfo
import ru.radiationx.data.entity.app.auth.SocialAuth
import ru.radiationx.data.entity.app.auth.SocialAuthException
import ru.radiationx.data.entity.app.other.ProfileItem
import ru.radiationx.shared.ktx.android.nullString
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
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

    fun loadUser(): Single<ProfileItem> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "user"
        )
        return client.post(apiConfig.apiUrl, args)
            .compose(ApiResponse.fetchResult<JSONObject>())
            .map { authParser.parseUser(it) }
    }

    fun loadOtpInfo(deviceId: String): Single<OtpInfo> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "auth_get_otp",
            "deviceId" to deviceId
        )
        return client
            .post(apiConfig.apiUrl, args)
            .compose(ApiResponse.fetchResult<JSONObject>())
            .onErrorResumeNext { Single.error(authParser.checkOtpError(it)) }
            .map { authParser.parseOtp(it) }
    }

    fun acceptOtp(code: String): Completable {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "auth_accept_otp",
            "code" to code
        )
        return client
            .post(apiConfig.apiUrl, args)
            .compose(ApiResponse.fetchResult<JSONObject>())
            .onErrorResumeNext { Single.error(authParser.checkOtpError(it)) }
            .ignoreElement()
    }

    fun signInOtp(code: String, deviceId: String): Single<ProfileItem> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "auth_login_otp",
            "deviceId" to deviceId,
            "code" to code
        )
        return client.post(apiConfig.apiUrl, args)
            .compose(ApiResponse.fetchResult<JSONObject>())
            .onErrorResumeNext { Single.error(authParser.checkOtpError(it)) }
            .flatMap { loadUser() }
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

        val fixedUrl = Uri.parse(apiConfig.baseUrl).host?.let { redirectDomain ->
            resultUrl.replace("www.anilibria.tv", redirectDomain)
        } ?: resultUrl

        return client
            .getFull(fixedUrl, args)
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