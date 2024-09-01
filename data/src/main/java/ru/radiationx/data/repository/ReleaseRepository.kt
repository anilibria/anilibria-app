package ru.radiationx.data.repository

import anilibria.api.releases.ReleasesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

/**
 * Created by radiationx on 17.12.17.
 */
class ReleaseRepository @Inject constructor(
    private val releaseApi: ReleasesApi,
    private val updateMiddleware: ReleaseUpdateMiddleware,
    private val apiUtils: ApiUtils,
    private val apiConfig: ApiConfig
) {

    private val searchIdRegex = Regex("^id(\\d{3,})\$")

    suspend fun getRandomRelease(): Release = withContext(Dispatchers.IO) {
        releaseApi
            .getRandomReleases(1)
            .first()
            .toDomain()
    }

    suspend fun getRelease(releaseId: ReleaseId): Release = withContext(Dispatchers.IO) {
        releaseApi
            .getRelease(releaseId.id.toString())
            .toDomain()
            .also { updateMiddleware.handle(it) }
    }

    suspend fun getRelease(releaseIdName: ReleaseCode): Release = withContext(Dispatchers.IO) {
        releaseApi
            .getRelease(releaseIdName.code)
            .toDomain()
            .also { updateMiddleware.handle(it) }
    }

    suspend fun getFullReleasesById(ids: List<ReleaseId>): List<Release> =
        withContext(Dispatchers.IO) {
            releaseApi
                .getReleasesByIds(ids.map { it.id })
                .map { it.toDomain() }
                .also { updateMiddleware.handle(it) }
        }

    suspend fun search(query: String): List<Release> = withContext(Dispatchers.IO) {
        val releaseId = getQueryId(query)
        if (releaseId != null) {
            val release = releaseApi
                .getRelease(releaseId.toString())
                .toDomain()
            listOf(release)
        } else {
            releaseApi
                .search(query)
                .map { it.toDomain() }
        }
    }

    private fun getQueryId(query: String): Int? {
        return searchIdRegex.find(query)?.let { matchResult ->
            matchResult.groupValues.getOrNull(1)?.toIntOrNull()
        }
    }
}
