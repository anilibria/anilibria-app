package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import ru.radiationx.data.datasource.SuspendMutableStateFlow
import ru.radiationx.data.datasource.holders.UserHolder
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

    private val userRelay = SuspendMutableStateFlow {
        getSavedUser()
    }

    override suspend fun getUser(): ProfileItem? {
        return userRelay.getValue()
    }

    override fun observeUser(): Flow<ProfileItem?> {
        return userRelay
    }

    override suspend fun saveUser(user: ProfileItem) {
        localSaveUser(user)
        updateState()
    }

    override suspend fun delete() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit {
                remove(KEY_SAVED_USER)
            }
        }
        updateState()
    }

    private suspend fun updateState() {
        userRelay.setValue(getSavedUser())
    }

    private suspend fun getSavedUser(): ProfileItem? {
        return withContext(Dispatchers.IO) {
            sharedPreferences
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
    }

    private suspend fun localSaveUser(user: ProfileItem) {
        withContext(Dispatchers.IO) {
            val userJson = JSONObject()
            userJson.put("id", user.id)
            userJson.put("nick", user.nick)
            userJson.put("avatar", user.avatarUrl)
            sharedPreferences.edit().putString(KEY_SAVED_USER, userJson.toString()).apply()
        }
    }
}