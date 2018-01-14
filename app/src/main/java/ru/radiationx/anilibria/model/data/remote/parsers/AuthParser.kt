package ru.radiationx.anilibria.model.data.remote.parsers

import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import java.util.regex.Pattern

/**
 * Created by radiationx on 31.12.17.
 */
class AuthParser(private val apiUtils: IApiUtils) {
    val patreonPattern = "<div[^>]*?id=\"bx_auth_serv_formPatreon\"[^>]*?>[^<]*?<a[^>]*?href=\"([^\"]*?)\"[^>]*?>"
    val vkPattern = "<div[^>]*?id=\"bx_auth_serv_formVKontakte\"[^>]*?>[^<]*?<a[^>]*?onclick=\"BX.util.popup\\(['\"]([^\"']*?)['\"]"

    val socialPatterns = arrayOf(patreonPattern, vkPattern)
    val userPattern = "<div[^>]*?class=\"[^\"]*?useravatar[^\"]*?\"[^>]*?>[^<]*?(?:<img[^>]*?src=\"([^\"]*?)\"[^>]*?>)?[^<]*?<\\/div>[^<]*?<div[^>]*?class=\"[^\"]*?userinfo[^\"]*?\"[^>]*?>[^<]*?<p[^>]*?>([\\s\\S]*?)<\\/p>[^<]*?<p>[^<]*?<a[^>]href=\"\\/user\\/(\\d+)[^\"]*?\"[^>]*?"


    fun authResult(responseText: String): ProfileItem {
        val user = ProfileItem()
        val matcher = Pattern.compile(userPattern).matcher(responseText)
        if (matcher.find()) {
            matcher.group(1)?.let {
                user.avatarUrl = Api.BASE_URL_IMAGES + it
            }
            user.nick = apiUtils.escapeHtml(matcher.group(2)).toString()
            user.id = matcher.group(3).toInt()
            user.authState = AuthState.AUTH
        } else {
            user.authState = AuthState.NO_AUTH
        }
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