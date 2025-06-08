package ru.radiationx.data.api.profile

import anilibria.api.profile.ProfileApi
import ru.radiationx.data.api.profile.mapper.toDomain
import ru.radiationx.data.api.profile.models.Profile
import javax.inject.Inject

class ProfileApiDataSource @Inject constructor(
    private val api: ProfileApi
) {

    suspend fun getProfile(): Profile {
        return api.getProfile().toDomain()
    }
}