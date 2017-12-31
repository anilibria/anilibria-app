package ru.radiationx.anilibria.model.data.remote.parsers

import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import java.util.regex.Pattern

/**
 * Created by radiationx on 31.12.17.
 */
class AuthParser(private val apiUtils: IApiUtils) {
    val patreonPattern = "<div[^>]*?id=\"bx_auth_serv_formPatreon\"[^>]*?>[^<]*?<a[^>]*?href=\"([^\"]*?)\"[^>]*?>"
    val vkPattern = "<div[^>]*?id=\"bx_auth_serv_formVKontakte\"[^>]*?>[^<]*?<a[^>]*?onclick=\"BX.util.popup\\(['\"]([^\"']*?)['\"]"

    val socialPatterns = arrayOf(patreonPattern, vkPattern)
    val userPattern = "<div[^>]*?class=\"[^\"]*?userinfo[^\"]*?\"[^>]*?>[^<]*?<p[^>]*?>([\\s\\S]*?)<\\/p>"


    fun authResult(responseText: String): AuthState {
        var user: String? = null
        val matcher = Pattern.compile(userPattern).matcher(responseText)
        if (matcher.find()) {
            user = matcher.group(1)
        }
        return if (user != null) AuthState.AUTH else AuthState.NO_AUTH
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