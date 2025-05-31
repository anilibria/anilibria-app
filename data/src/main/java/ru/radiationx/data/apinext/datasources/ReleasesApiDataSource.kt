package ru.radiationx.data.apinext.datasources

import anilibria.api.releases.ReleasesApi
import kotlinx.coroutines.ensureActive
import ru.radiationx.data.apinext.models.ReleaseMember
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.apinext.toRequestIdentifier
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.types.ReleaseIdentifier
import toothpick.InjectConstructor
import kotlin.coroutines.coroutineContext

@InjectConstructor
class ReleasesApiDataSource(
    private val api: ReleasesApi
) {

    suspend fun getLatestReleases(limit: Int?): List<Release> {
        return api.getLatestReleases(limit).map { it.toDomain() }
    }

    suspend fun getRandomReleases(limit: Int?): List<Release> {
        return api.getRandomReleases(limit).map { it.toDomain() }
    }

    suspend fun getReleases(
        identifiers: List<ReleaseIdentifier>,
    ): List<Release> {
        var currentPage = 1
        val loadedReleases = mutableListOf<Release>()
        while (true) {
            coroutineContext.ensureActive()
            val paginated = getReleasesPaginated(
                identifiers = identifiers,
                page = currentPage,
                limit = null
            )
            loadedReleases.addAll(paginated.data)
            if (paginated.isEnd()) {
                break
            }
            currentPage++
        }
        return loadedReleases.sortByIdentifierOrder(identifiers)
    }

    suspend fun getRelease(identifier: ReleaseIdentifier): Release {
        return api.getRelease(identifier.toRequestIdentifier()).toDomain()
    }

    suspend fun getMembers(identifier: ReleaseIdentifier): List<ReleaseMember> {
        return api.getMembers(identifier.toRequestIdentifier()).map { it.toDomain() }
    }

    // todo API2 migrate to universal episode
    /*suspend fun getEpisode(releaseId: ReleaseId, episodeUUID: EpisodeUUID): Episode {
        return api.getEpisode(episodeUUID.uuid).let { response ->
            response.toEpisode(releaseId)
        }
    }*/

    suspend fun search(query: String): List<Release> {
        return api.search(query).map { it.toDomain() }
    }

    private suspend fun getReleasesPaginated(
        identifiers: List<ReleaseIdentifier>,
        page: Int?,
        limit: Int?
    ): Paginated<Release> {
        val requestIds = identifiers
            .filterIsInstance<ReleaseId>()
            .joinToString(",") { it.toRequestIdentifier() }
        val requestCodes = identifiers
            .filterIsInstance<ReleaseCode>()
            .joinToString(",") { it.toRequestIdentifier() }
        return api
            .getReleases(ids = requestIds, aliases = requestCodes, page = page, limit = limit)
            .toDomain { it.toDomain() }
    }

    private fun List<Release>.sortByIdentifierOrder(
        identifiers: List<ReleaseIdentifier>
    ): List<Release> {
        val releases = this
        val indexMap = buildMap {
            releases.forEach { release ->
                put(release.id, release)
                put(release.code, release)
            }
        }
        return identifiers.mapNotNull { indexMap[it] }
    }
}