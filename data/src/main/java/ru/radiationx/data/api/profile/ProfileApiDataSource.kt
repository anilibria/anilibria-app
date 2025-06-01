package ru.radiationx.data.api.profile

import anilibria.api.profile.ProfileApi
import ru.radiationx.data.api.profile.mapper.toDomain
import ru.radiationx.data.api.profile.models.Profile
import toothpick.InjectConstructor

@InjectConstructor
class ProfileApiDataSource(
    private val api: ProfileApi
) {

    suspend fun getProfile(): Profile {
        return api.getProfile().toDomain()
    }
}