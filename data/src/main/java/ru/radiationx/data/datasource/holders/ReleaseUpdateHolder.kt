package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.ReleaseUpdate
import ru.radiationx.data.entity.domain.types.ReleaseId

interface ReleaseUpdateHolder {
    fun observeEpisodes(): Flow<List<ReleaseUpdate>>
    suspend fun getReleases(): List<ReleaseUpdate>
    fun getRelease(id: ReleaseId): ReleaseUpdate?
    fun viewRelease(release: Release)
    fun putInitialRelease(releases: List<Release>)
}