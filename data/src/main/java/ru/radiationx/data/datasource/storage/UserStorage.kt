package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import ru.radiationx.data.datasource.holders.UserHolder
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.entity.domain.other.ProfileItem
import javax.inject.Inject

/**
 * Created by radiationx on 11.01.18.
 */
class UserStorage @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : UserHolder {

    companion object {
        private const val KEY_SAVED_USER = "saved_user_v2"
    }

    private val userRelay by lazy { MutableStateFlow(getSavedUser()) }

    override fun getUser(): ProfileItem? {
        return userRelay.value
    }

    override fun observeUser(): Flow<ProfileItem?> {
        return userRelay.asStateFlow()
    }

    override fun saveUser(user: ProfileItem) {
        localSaveUser(user)
        updateState()
    }

    override fun delete() {
        sharedPreferences.edit {
            remove(KEY_SAVED_USER)
        }
        updateState()
    }

    private fun updateState() {
        userRelay.value = getSavedUser()
    }

    private fun getSavedUser(): ProfileItem? {
        return sharedPreferences
            .getString(KEY_SAVED_USER, null)
            ?.let { JSONObject(it) }
            ?.let { userJson ->
                ProfileItem(
                    id = userJson.getInt("id"),
                    nick = userJson.getString("nick"),
                    avatarUrl = userJson.getString("avatar"),
                )
            }
    }

    private fun localSaveUser(user: ProfileItem) {
        val userJson = JSONObject()
        userJson.put("id", user.id)
        userJson.put("nick", user.nick)
        userJson.put("avatar", user.avatarUrl)
        sharedPreferences.edit().putString(KEY_SAVED_USER, userJson.toString()).apply()
    }
}