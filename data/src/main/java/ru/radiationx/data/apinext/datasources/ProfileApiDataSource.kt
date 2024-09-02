package ru.radiationx.data.apinext.datasources

import anilibria.api.profile.ProfileApi
import ru.radiationx.data.apinext.models.Profile
import ru.radiationx.data.apinext.toDomain
import toothpick.InjectConstructor

@InjectConstructor
class ProfileApiDataSource(
    private val api: ProfileApi
) {

    suspend fun getProfile(): Profile {
        return api.getProfile().toDomain()
    }
}