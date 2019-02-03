package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.profile.Profile
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.ProfileParser

/**
 * Created by radiationx on 03.01.18.
 */
class ProfileApi(
        private val client: IClient,
        private val profileParser: ProfileParser
) {

    fun loadProfile(userId: Int): Single<Profile> {
        val args: MutableMap<String, String> = mutableMapOf()
        val url = "${Api.BASE_URL}/user/$userId"
        return client.get(url, args).map { profileParser.profile(it) }
    }
}