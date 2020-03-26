package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.data.datasource.remote.ApiError
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.app.auth.SocialAuth
import ru.radiationx.data.entity.app.other.ProfileItem
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.shared.ktx.android.nullString
import javax.inject.Inject

/**
 * Created by radiationx on 31.12.17.
 */
class AuthParser @Inject constructor(
    private val apiUtils: IApiUtils,
    private val apiConfig: ApiConfig
) {

    fun authResult(responseText: String): String {
        val responseJson = JSONObject(responseText)
        val error = responseJson.nullString("err")
        val message = responseJson.nullString("mes")
        val key = responseJson.nullString("key")
        if (error != "ok" && key != "authorized") {
            throw ApiError(400, message ?: key, null)
        }
        return message.orEmpty()
    }

    fun parseUser(responseJson: JSONObject): ProfileItem {
        val user = ProfileItem()
        user.id = responseJson.getInt("id")
        user.nick = responseJson.nullString("login").orEmpty()
        user.avatarUrl = responseJson.nullString("avatar")?.let {
            "${apiConfig.baseImagesUrl}$it"
        }
        user.authState = AuthState.AUTH
        return user
    }

    fun parseSocialAuth(responseJson: JSONArray): List<SocialAuth> {
        val resultItems = mutableListOf<SocialAuth>()
        for (j in 0 until responseJson.length()) {
            val jsonItem = responseJson.getJSONObject(j)
            resultItems.add(
                SocialAuth(
                    jsonItem.getString("key"),
                    jsonItem.getString("title"),
                    jsonItem.getString("socialUrl"),
                    jsonItem.getString("resultPattern"),
                    jsonItem.getString("errorUrlPattern")
                )
            )
        }
        return resultItems
    }

}