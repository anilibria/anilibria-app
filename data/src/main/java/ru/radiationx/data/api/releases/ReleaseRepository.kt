package ru.radiationx.data.api.releases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.releases.models.Suggestions
import ru.radiationx.data.app.releaseupdate.ReleaseUpdateMiddleware
import ru.radiationx.data.common.ReleaseCode
import ru.radiationx.data.common.ReleaseId
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
            .getRandomReleases(5)
            .random()
    }

    suspend fun getReleaseByCode(code: ReleaseCode): Release = withContext(Dispatchers.IO) {
        releaseApi.getReleaseByCode(code)
    }

    suspend fun getRelease(id: ReleaseId): Release = withContext(Dispatchers.IO) {
        releaseApi
            .getRelease(id)
            .also { updateMiddleware.handle(it) }
    }

    suspend fun getFullReleasesById(ids: List<ReleaseId>): List<Release> =
        withContext(Dispatchers.IO) {
            releaseApi
                .getReleases(ids)
                .also { updateMiddleware.handle(it) }
        }

    suspend fun search(query: String): Suggestions = withContext(Dispatchers.IO) {
        val releaseId = getQueryId(query)
        val releases = if (releaseId != null) {
            val release = releaseApi.getRelease(ReleaseId(releaseId))
            listOf(release)
        } else {
            releaseApi.search(query)
        }
        Suggestions(query, releases)
    }

    private fun getQueryId(query: String): Int? {
        return searchIdRegex.find(query)?.let { matchResult ->
            matchResult.groupValues.getOrNull(1)?.toIntOrNull()
        }
    }
}
