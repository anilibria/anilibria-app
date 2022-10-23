package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.release.ReleaseUpdate

interface ReleaseUpdateHolder {
    fun observeEpisodes(): Flow<List<ReleaseUpdate>>
    suspend fun getReleases(): List<ReleaseUpdate>
    fun getRelease(id: Int): ReleaseUpdate?
    fun updRelease(release: ReleaseUpdate)
    fun updAllRelease(releases: List<ReleaseUpdate>)
    fun putRelease(release: ReleaseItem)
    fun putAllRelease(releases: List<ReleaseItem>)
}