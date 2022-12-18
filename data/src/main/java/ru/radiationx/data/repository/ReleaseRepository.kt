package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.ReleaseApi
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.RandomRelease
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
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

    suspend fun getRandomRelease(): RandomRelease = withContext(Dispatchers.IO) {
        releaseApi
            .getRandomRelease()
            .toDomain()
    }

    suspend fun getRelease(releaseId: ReleaseId): Release = withContext(Dispatchers.IO) {
        releaseApi
            .getRelease(releaseId.id)
            .toDomain(apiUtils, apiConfig)
            .also { updateMiddleware.handle(it) }
    }

    suspend fun getRelease(releaseIdName: ReleaseCode): Release = withContext(Dispatchers.IO) {
        releaseApi
            .getRelease(releaseIdName.code)
            .toDomain(apiUtils, apiConfig)
            .also { updateMiddleware.handle(it) }
    }

    suspend fun getReleasesById(ids: List<Int>): List<Release> = withContext(Dispatchers.IO) {
        releaseApi
            .getReleasesByIds(ids)
            .map { it.toDomain(apiUtils, apiConfig) }
            .also { updateMiddleware.handle(it) }
    }

    suspend fun getReleases(page: Int): Paginated<Release> = withContext(Dispatchers.IO) {
        releaseApi
            .getReleases(page)
            .toDomain { it.toDomain(apiUtils, apiConfig) }
            .also { updateMiddleware.handle(it.data) }
    }
}
