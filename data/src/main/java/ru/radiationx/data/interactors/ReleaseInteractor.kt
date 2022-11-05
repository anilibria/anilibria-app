package ru.radiationx.data.interactors

import kotlinx.coroutines.flow.*
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.app.release.EpisodeAccess
import ru.radiationx.data.entity.app.release.RandomRelease
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.repository.ReleaseRepository
import javax.inject.Inject

/**
 * Created by radiationx on 17.02.18.
 */
class ReleaseInteractor @Inject constructor(
    private val releaseRepository: ReleaseRepository,
    private val episodesCheckerStorage: EpisodesCheckerHolder,
    private val preferencesHolder: PreferencesHolder,
) {

    private val checkerCombiner: (suspend (ReleaseItem, List<EpisodeAccess>) -> ReleaseItem) =
        { release, episodeAccesses ->
            val newEpisodes = release.episodes.map { episode ->
                val episodeAccess = episodeAccesses.firstOrNull {
                    it.releaseId == episode.releaseId && it.id == episode.id
                }
                if (episodeAccess != null) {
                    episode.copy(access = episodeAccess)
                } else {
                    episode
                }
            }
            release.copy(episodes = newEpisodes)
        }

    private val releaseItems = MutableStateFlow<List<ReleaseItem>>(emptyList())
    private val releases = MutableStateFlow<List<ReleaseItem>>(emptyList())

    suspend fun getRandomRelease(): RandomRelease = releaseRepository.getRandomRelease()

    private suspend fun loadRelease(releaseId: Int): ReleaseItem {
        return releaseRepository.getRelease(releaseId).also(::updateFullCache)
    }

    private suspend fun loadRelease(releaseCode: String): ReleaseItem {
        return releaseRepository.getRelease(releaseCode).also(::updateFullCache)
    }

    suspend fun loadRelease(releaseId: Int = -1, releaseCode: String? = null): ReleaseItem {
        return when {
            releaseId != -1 -> loadRelease(releaseId)
            releaseCode != null -> loadRelease(releaseCode)
            else -> throw Exception("Unknown id=$releaseId or code=$releaseCode")
        }
    }

    fun getItem(releaseId: Int = -1, releaseCode: String? = null): ReleaseItem? {
        return releaseItems.value.findRelease(releaseId, releaseCode)
    }

    fun getFull(releaseId: Int = -1, releaseCode: String? = null): ReleaseItem? {
        return releases.value.findRelease(releaseId, releaseCode)
    }

    fun observeItem(releaseId: Int = -1, releaseCode: String? = null): Flow<ReleaseItem> {
        return releaseItems.mapNotNull { it.findRelease(releaseId, releaseCode) }
    }

    fun observeFull(releaseId: Int = -1, releaseCode: String? = null): Flow<ReleaseItem> {
        return combine(
            releases.mapNotNull { it.findRelease(releaseId, releaseCode) },
            episodesCheckerStorage.observeEpisodes(),
            checkerCombiner
        )
    }

    fun updateItemsCache(items: List<ReleaseItem>) {
        releaseItems.update { releaseItems ->
            releaseItems.filterNot { release ->
                items.any {
                    check(release, it.id, it.code)
                }
            } + items
        }
    }

    fun updateFullCache(release: ReleaseItem) {
        releases.update { releases ->
            releases.filterNot {
                check(it, release.id, release.code)
            } + release
        }
    }

    /* Common */
    fun putEpisode(episode: EpisodeAccess) = episodesCheckerStorage.putEpisode(episode)

    fun putEpisodes(episodes: List<EpisodeAccess>) =
        episodesCheckerStorage.putAllEpisode(episodes)

    fun getEpisodes(releaseId: Int) = episodesCheckerStorage.getEpisodes(releaseId)

    fun resetEpisodesHistory(releaseId: Int) {
        episodesCheckerStorage.remove(releaseId)
    }

    fun getQuality() = preferencesHolder.getQuality()

    fun setQuality(value: Int) = preferencesHolder.setQuality(value)

    fun observeQuality() = preferencesHolder.observeQuality()

    fun getPlayerType() = preferencesHolder.getPlayerType()

    fun setPlayerType(value: Int) = preferencesHolder.setPlayerType(value)

    fun getPlaySpeed() = preferencesHolder.playSpeed

    fun setPlaySpeed(value: Float) {
        preferencesHolder.playSpeed = value
    }

    fun observePlaySpeed(): Flow<Float> = preferencesHolder.observePlaySpeed()

    fun getPIPControl() = preferencesHolder.pipControl

    fun setPIPControl(value: Int) {
        preferencesHolder.pipControl = value
    }

    private fun Int.idOrNull(limit: Int = -1): Int? = if (this > limit) {
        this
    } else {
        null
    }

    private fun <T : ReleaseItem> List<T>.findRelease(id: Int, code: String?): T? = find {
        check(it, id, code)
    }

    private fun <T : ReleaseItem> check(release: T, id: Int, code: String?): Boolean {
        val nullId = id.idOrNull()
        val releaseNullId = release.id.idOrNull()
        val releaseCode = release.code
        val foundById = releaseNullId != null && nullId != null && releaseNullId == nullId
        val foundByCode = releaseCode != null && code != null && releaseCode == code
        return foundById || foundByCode
    }

}