package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.domain.other.ProfileItem

/**
 * Created by radiationx on 11.01.18.
 */
interface UserHolder {
    suspend fun getUser(): ProfileItem?

    fun observeUser(): Flow<ProfileItem?>

    suspend fun saveUser(user: ProfileItem)

    suspend fun delete()
}