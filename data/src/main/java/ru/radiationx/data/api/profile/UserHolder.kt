package ru.radiationx.data.api.profile

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.api.profile.models.User

/**
 * Created by radiationx on 11.01.18.
 */
interface UserHolder {
    suspend fun getUser(): User?

    fun observeUser(): Flow<User?>

    suspend fun saveUser(user: User)

    suspend fun delete()
}