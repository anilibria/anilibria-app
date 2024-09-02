package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import ru.radiationx.data.apinext.models.User
import ru.radiationx.data.datasource.SuspendMutableStateFlow
import ru.radiationx.data.datasource.holders.UserHolder
import ru.radiationx.data.entity.domain.types.UserId
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

    override suspend fun getUser(): User? {
        return userRelay.getValue()
    }

    override fun observeUser(): Flow<User?> {
        return userRelay
    }

    override suspend fun saveUser(user: User) {
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

    private suspend fun getSavedUser(): User? {
        return withContext(Dispatchers.IO) {
            sharedPreferences
                .getString(KEY_SAVED_USER, null)
                ?.let { JSONObject(it) }
                ?.let { userJson ->
                    User(
                        id = UserId(userJson.getInt("id")),
                        nickname = userJson.getString("nick"),
                        avatar = userJson.optString("avatar").ifEmpty { null },
                    )
                }
        }
    }

    private suspend fun localSaveUser(user: User) {
        withContext(Dispatchers.IO) {
            val userJson = JSONObject()
            userJson.put("id", user.id.id)
            userJson.put("nick", user.nickname)
            userJson.put("avatar", user.avatar)
            sharedPreferences.edit().putString(KEY_SAVED_USER, userJson.toString()).apply()
        }
    }
}