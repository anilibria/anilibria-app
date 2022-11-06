package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import ru.radiationx.data.datasource.holders.UserHolder
import ru.radiationx.data.entity.domain.other.ProfileItem
import ru.radiationx.data.entity.common.AuthState
import javax.inject.Inject

/**
 * Created by radiationx on 11.01.18.
 */
class UserStorage @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : UserHolder {

    private val userRelay by lazy { MutableStateFlow(getSavedUser()) }

    override fun getUser(): ProfileItem = userRelay.value

    private fun getSavedUser(): ProfileItem {
        val userSource = sharedPreferences.getString("saved_user", null)
        return if (userSource == null) {
            val user = ProfileItem()
            localSaveUser(user)
            user
        } else {
            val userJson = JSONObject(userSource)
            ProfileItem(
                id = userJson.getInt("id"),
                nick = userJson.getString("nick"),
                avatarUrl = userJson.getString("avatar"),
                authState = AuthState.valueOf(userJson.getString("authState")),
            )
        }
    }

    private fun localSaveUser(user: ProfileItem) {
        val userJson = JSONObject()
        userJson.put("authState", user.authState.toString())
        userJson.put("id", user.id)
        userJson.put("nick", user.nick)
        userJson.put("avatar", user.avatarUrl)
        sharedPreferences.edit().putString("saved_user", userJson.toString()).apply()
    }

    override fun observeUser(): Flow<ProfileItem> = userRelay

    override fun saveUser(user: ProfileItem) {
        localSaveUser(user)
        userRelay.value = user
    }

    override fun delete() {
        val user = ProfileItem(authState = AuthState.AUTH_SKIPPED)
        saveUser(user)
    }

}