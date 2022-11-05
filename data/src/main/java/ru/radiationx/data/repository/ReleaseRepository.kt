package ru.radiationx.data.repository

import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.ReleaseApi
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.RandomRelease
import ru.radiationx.data.entity.app.release.Release
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

/**
 * Created by radiationx on 17.12.17.
 */
class ReleaseRepository @Inject constructor(
    private val releaseApi: ReleaseApi,
    private val updateMiddleware: ReleaseUpdateMiddleware,
    private val apiUtils: ApiUtils,
    private val apiConfig: ApiConfig
) {

    suspend fun getRandomRelease(): RandomRelease = releaseApi
        .getRandomRelease()
        .toDomain()

    suspend fun getRelease(releaseId: Int): Release = releaseApi
        .getRelease(releaseId)
        .toDomain(apiUtils, apiConfig)
        .also { updateMiddleware.handle(it) }

    suspend fun getRelease(releaseIdName: String): Release = releaseApi
        .getRelease(releaseIdName)
        .toDomain(apiUtils, apiConfig)
        .also { updateMiddleware.handle(it) }

    suspend fun getReleasesById(ids: List<Int>): List<Release> = releaseApi
        .getReleasesByIds(ids)
        .map { it.toDomain(apiUtils, apiConfig) }
        .also { updateMiddleware.handle(it) }

    suspend fun getReleases(page: Int): Paginated<Release> = releaseApi
        .getReleases(page)
        .toDomain { it.toDomain(apiUtils, apiConfig) }
        .also { updateMiddleware.handle(it.data) }
}
