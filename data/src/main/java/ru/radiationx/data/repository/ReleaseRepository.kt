package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.apinext.datasources.ReleasesApiDataSource
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import javax.inject.Inject

/**
 * Created by radiationx on 17.12.17.
 */
class ReleaseRepository @Inject constructor(
    private val releaseApi: ReleasesApiDataSource,
    private val updateMiddleware: ReleaseUpdateMiddleware
) {

    private val searchIdRegex = Regex("^id(\\d{3,})\$")

    suspend fun getRandomRelease(): Release = withContext(Dispatchers.IO) {
        releaseApi
            .getRandomReleases(1)
            .first()
    }

    suspend fun getRelease(releaseId: ReleaseId): Release = withContext(Dispatchers.IO) {
        releaseApi
            .getRelease(releaseId)
            .also { updateMiddleware.handle(it) }
    }

    suspend fun getRelease(code: ReleaseCode): Release = withContext(Dispatchers.IO) {
        releaseApi
            .getRelease(code)
            .also { updateMiddleware.handle(it) }
    }

    suspend fun getFullReleasesById(ids: List<ReleaseId>): List<Release> =
        withContext(Dispatchers.IO) {
            releaseApi
                .getReleasesByIds(ids)
                .also { updateMiddleware.handle(it) }
        }

    suspend fun search(query: String): List<Release> = withContext(Dispatchers.IO) {
        val releaseId = getQueryId(query)
        if (releaseId != null) {
            val release = releaseApi.getRelease(ReleaseId(releaseId))
            listOf(release)
        } else {
            releaseApi.search(query)
        }
    }

    private fun getQueryId(query: String): Int? {
        return searchIdRegex.find(query)?.let { matchResult ->
            matchResult.groupValues.getOrNull(1)?.toIntOrNull()
        }
    }
}
