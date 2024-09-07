package ru.radiationx.data.apinext.datasources

import anilibria.api.releases.ReleasesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.shared.ktx.coRunCatching
import toothpick.InjectConstructor

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

    suspend fun getRelease(id: ReleaseId): Release {
        return api.getRelease(id.id.toString()).toDomain()
    }

    suspend fun getRelease(code: ReleaseCode): Release {
        return api.getRelease(code.code).toDomain()
    }

    suspend fun search(query: String): List<Release> {
        return api.search(query).map { it.toDomain() }
    }

    suspend fun getReleasesByIds(ids: List<ReleaseId>): List<Release> {
        return coroutineScope {
            val requests = ids.map { id ->
                async {
                    coRunCatching { getRelease(id) }
                }
            }
            requests.awaitAll().mapNotNull { it.getOrNull() }
        }
    }
}