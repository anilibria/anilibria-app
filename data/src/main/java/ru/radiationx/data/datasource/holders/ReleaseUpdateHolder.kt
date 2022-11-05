package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.app.release.Release
import ru.radiationx.data.entity.app.release.ReleaseUpdate

interface ReleaseUpdateHolder {
    fun observeEpisodes(): Flow<List<ReleaseUpdate>>
    suspend fun getReleases(): List<ReleaseUpdate>
    fun getRelease(id: Int): ReleaseUpdate?
    fun viewRelease(release: Release)
    fun putInitialRelease(releases: List<Release>)
}