package ru.radiationx.data.datasource.remote.api

import android.net.Uri
import com.squareup.moshi.Moshi
import org.json.JSONObject
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.*
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.parsers.AuthParser
import ru.radiationx.data.entity.app.auth.SocialAuth
import ru.radiationx.data.entity.app.auth.SocialAuthException
import ru.radiationx.data.entity.response.auth.OtpInfoResponse
import ru.radiationx.data.entity.response.auth.SocialAuthResponse
import ru.radiationx.data.entity.response.other.ProfileResponse
import ru.radiationx.shared.ktx.android.nullString
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class AuthApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val authParser: AuthParser,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi
) {

    suspend fun loadUser(): ProfileResponse {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "user"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse<ProfileResponse>(moshi)
    }

    suspend fun loadOtpInfo(deviceId: String): OtpInfoResponse {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "auth_get_otp",
            "deviceId" to deviceId
        )
        return try {
            client
                .post(apiConfig.apiUrl, args)
                .fetchApiResponse(moshi)
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
                .fetchEmptyApiResponse(moshi)
        } catch (ex: Throwable) {
            throw authParser.checkOtpError(ex)
        }
    }

    suspend fun signInOtp(code: String, deviceId: String): ProfileResponse {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "auth_login_otp",
            "deviceId" to deviceId,
            "code" to code
        )
        return try {
            client
                .post(apiConfig.apiUrl, args)
                .fetchEmptyApiResponse(moshi)
                .let { loadUser() }
        } catch (ex: Throwable) {
            throw authParser.checkOtpError(ex)
        }
    }

    suspend fun signIn(login: String, password: String, code2fa: String): ProfileResponse {
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

    suspend fun loadSocialAuth(): List<SocialAuthResponse> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "social_auth"
        )
        return client
            .post(apiConfig.apiUrl, args)
            .fetchListApiResponse(moshi)
    }

    suspend fun signInSocial(resultUrl: String, item: SocialAuth): ProfileResponse {
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