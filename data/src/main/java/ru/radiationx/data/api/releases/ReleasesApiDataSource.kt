package ru.radiationx.data.api.releases

import anilibria.api.releases.ReleasesApi
import kotlinx.coroutines.ensureActive
import ru.radiationx.data.api.releases.mapper.toComboEpisode
import ru.radiationx.data.api.releases.mapper.toDomain
import ru.radiationx.data.api.releases.models.ComboEpisode
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.releases.models.ReleaseMember
import ru.radiationx.data.api.shared.pagination.Paginated
import ru.radiationx.data.api.shared.pagination.toDomain
import ru.radiationx.data.api.views.mapper.toDomain
import ru.radiationx.data.api.views.models.ViewHistory
import ru.radiationx.data.common.EpisodeUUID
import ru.radiationx.data.common.ReleaseAlias
import ru.radiationx.data.common.ReleaseId
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class ReleasesApiDataSource @Inject constructor(
    private val api: ReleasesApi
) {

    suspend fun getLatestReleases(limit: Int?): List<Release> {
        return api.getLatestReleases(limit).map { it.toDomain() }
    }

    suspend fun getRandomReleases(limit: Int?): List<Release> {
        return api.getRandomReleases(limit).map { it.toDomain() }
    }

    suspend fun getRecommendedReleases(limit: Int?, releaseId: ReleaseId?): List<Release> {
        return api.getRecommendedReleases(limit, releaseId?.id).map { it.toDomain() }
    }

    suspend fun getReleases(ids: List<ReleaseId>): List<Release> {
        var currentPage = 1
        val loadedReleases = mutableListOf<Release>()
        while (true) {
            coroutineContext.ensureActive()
            val paginated = getReleasesPaginated(
                ids = ids,
                page = currentPage,
                limit = null
            )
            loadedReleases.addAll(paginated.data)
            if (paginated.isEnd()) {
                break
            }
            currentPage++
        }
        return loadedReleases.sortByIdsOrder(ids)
    }

    suspend fun getReleaseByAlias(alias: ReleaseAlias): Release {
        return api.getRelease(alias.alias).toDomain()
    }

    suspend fun getRelease(id: ReleaseId): Release {
        return api.getRelease(id.id.toString()).toDomain()
    }

    suspend fun getMembers(id: ReleaseId): List<ReleaseMember> {
        return api.getMembers(id.id.toString()).map { it.toDomain() }
    }

    suspend fun getViewHistory(id: ReleaseId): List<ViewHistory> {
        return api.getReleaseViewHistory(id.id.toString()).map { it.toDomain() }
    }

    suspend fun getEpisode(episodeUUID: EpisodeUUID): ComboEpisode {
        return api.getEpisode(episodeUUID.uuid).toComboEpisode()
    }

    suspend fun getTimeCode(episodeId: EpisodeUUID): ViewHistory {
        return api.getEpisodeViewHistory(episodeId.uuid).toDomain()
    }

    suspend fun search(query: String): List<Release> {
        return api.search(query).map { it.toDomain() }
    }

    private suspend fun getReleasesPaginated(
        ids: List<ReleaseId>,
        page: Int?,
        limit: Int?
    ): Paginated<Release> {
        val requestIds = ids.joinToString(",") { it.id.toString() }
        return api
            .getReleases(ids = requestIds, aliases = null, page = page, limit = limit)
            .toDomain { it.toDomain() }
    }

    private fun List<Release>.sortByIdsOrder(ids: List<ReleaseId>): List<Release> {
        val releases = this
        val indexMap = buildMap<ReleaseId, Release> {
            releases.forEach { release ->
                put(release.id, release)
            }
        }
        return ids.mapNotNull { indexMap[it] }
    }
}