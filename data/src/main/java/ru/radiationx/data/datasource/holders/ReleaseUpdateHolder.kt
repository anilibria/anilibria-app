package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.ReleaseUpdate
import ru.radiationx.data.entity.domain.types.ReleaseId

interface ReleaseUpdateHolder {
    fun observeEpisodes(): Flow<List<ReleaseUpdate>>
    suspend fun getReleases(): List<ReleaseUpdate>
    suspend fun getRelease(id: ReleaseId): ReleaseUpdate?
    suspend fun viewRelease(release: Release)
    suspend fun putInitialRelease(releases: List<Release>)
    suspend fun putAllRelease(releases: List<ReleaseUpdate>)
}