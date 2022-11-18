package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.domain.other.ProfileItem

/**
 * Created by radiationx on 11.01.18.
 */
interface UserHolder {
    fun getUser(): ProfileItem?

    fun observeUser(): Flow<ProfileItem?>

    fun saveUser(user: ProfileItem)

    fun delete()
}