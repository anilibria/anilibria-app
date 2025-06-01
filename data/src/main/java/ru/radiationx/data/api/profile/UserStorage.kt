package ru.radiationx.data.api.profile

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.radiationx.data.api.profile.db.UserDb
import ru.radiationx.data.api.profile.mapper.toDb
import ru.radiationx.data.api.profile.mapper.toDomain
import ru.radiationx.data.api.profile.models.User
import ru.radiationx.shared.ktx.android.SuspendMutableStateFlow
import javax.inject.Inject

/**
 * Created by radiationx on 11.01.18.
 */
class UserStorage @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi
) : UserHolder {

    companion object {
        private const val KEY_SAVED_USER = "saved_user_v3"
    }

    private val dataAdapter by lazy {
        moshi.adapter(UserDb::class.java)
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
                ?.let { dataAdapter.fromJson(it) }
                ?.toDomain()
        }
    }

    private suspend fun localSaveUser(user: User) {
        withContext(Dispatchers.IO) {
            val userJson = dataAdapter.toJson(user.toDb())
            sharedPreferences.edit().putString(KEY_SAVED_USER, userJson).apply()
        }
    }
}