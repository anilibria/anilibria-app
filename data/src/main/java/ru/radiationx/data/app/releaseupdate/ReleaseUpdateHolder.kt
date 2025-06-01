package ru.radiationx.data.app.releaseupdate

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.app.releaseupdate.models.ReleaseUpdate
import ru.radiationx.data.common.ReleaseId

interface ReleaseUpdateHolder {
    fun observeEpisodes(): Flow<List<ReleaseUpdate>>
    suspend fun getReleases(): List<ReleaseUpdate>
    suspend fun getRelease(id: ReleaseId): ReleaseUpdate?
    suspend fun viewRelease(release: Release)
    suspend fun putInitialRelease(releases: List<Release>)
    suspend fun putAllRelease(releases: List<ReleaseUpdate>)
}