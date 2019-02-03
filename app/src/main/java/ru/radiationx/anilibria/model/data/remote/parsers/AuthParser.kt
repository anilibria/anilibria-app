package ru.radiationx.anilibria.model.data.remote.parsers

import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.extension.nullString
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiError
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import java.util.regex.Pattern

/**
 * Created by radiationx on 31.12.17.
 */
class AuthParser(private val apiUtils: IApiUtils) {
    private val patreonPattern = "<div[^>]*?id=\"bx_auth_serv_formPatreon\"[^>]*?>[^<]*?<a[^>]*?href=\"([^\"]*?)\"[^>]*?>"
    private val vkPattern = "<div[^>]*?id=\"bx_auth_serv_formVKontakte\"[^>]*?>[^<]*?<a[^>]*?onclick=\"BX.util.popup\\(['\"]([^\"']*?)['\"]"

    private val socialPatterns = arrayOf(patreonPattern, vkPattern)
    private val userPattern = "<div[^>]*?class=\"[^\"]*?useravatar[^\"]*?\"[^>]*?>[^<]*?(?:<img[^>]*?src=\"([^\"]*?)\"[^>]*?>)?[^<]*?<\\/div>[^<]*?<div[^>]*?class=\"[^\"]*?userinfo[^\"]*?\"[^>]*?>[^<]*?<p[^>]*?>([\\s\\S]*?)<\\/p>[^<]*?<p>[^<]*?<a[^>]href=\"\\/user\\/(\\d+)[^\"]*?\"[^>]*?"

    fun authResult(responseText: String): String {
        val responseJson = JSONObject(responseText)
        val error = responseJson.nullString("err")
        val message = responseJson.nullString("mes")
        val key = responseJson.nullString("key")
        if (error != "ok" && key != "authorized") {
            throw ApiError(400, message, null)
        }
        return message.orEmpty()
    }

    fun parseUser(responseJson: JSONObject): ProfileItem {
        val user = ProfileItem()
        user.id = responseJson.getInt("id")
        user.nick = responseJson.nullString("login").orEmpty()
        user.avatarUrl = responseJson.nullString("avatar")?.let {
            "${Api.BASE_URL_IMAGES}$it"
        }
        user.authState = AuthState.AUTH
        return user
    }

    fun getSocialLinks(responseText: String): List<String> {
        val result = mutableListOf<String>()
        socialPatterns.forEach { pattern ->
            val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(responseText)
            if (matcher.find()) {
                result.add(matcher.group(1).replace("&amp;".toRegex(), "&"))
            }
        }
        return result
    }
}