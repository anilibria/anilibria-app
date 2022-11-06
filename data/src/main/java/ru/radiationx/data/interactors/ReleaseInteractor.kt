package ru.radiationx.data.interactors

import kotlinx.coroutines.flow.*
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.release.RandomRelease
import ru.radiationx.data.entity.domain.release.Release
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

    private val checkerCombiner: (suspend (Release, List<EpisodeAccess>) -> Release) =
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

    private val releaseItems = MutableStateFlow<List<Release>>(emptyList())
    private val releases = MutableStateFlow<List<Release>>(emptyList())

    suspend fun getRandomRelease(): RandomRelease = releaseRepository.getRandomRelease()

    private suspend fun loadRelease(releaseId: Int): Release {
        return releaseRepository.getRelease(releaseId).also(::updateFullCache)
    }

    private suspend fun loadRelease(releaseCode: String): Release {
        return releaseRepository.getRelease(releaseCode).also(::updateFullCache)
    }

    suspend fun loadRelease(releaseId: Int = -1, releaseCode: String? = null): Release {
        return when {
            releaseId != -1 -> loadRelease(releaseId)
            releaseCode != null -> loadRelease(releaseCode)
            else -> throw Exception("Unknown id=$releaseId or code=$releaseCode")
        }
    }

    fun getItem(releaseId: Int = -1, releaseCode: String? = null): Release? {
        return releaseItems.value.findRelease(releaseId, releaseCode)
    }

    fun getFull(releaseId: Int = -1, releaseCode: String? = null): Release? {
        return releases.value.findRelease(releaseId, releaseCode)
    }

    fun observeItem(releaseId: Int = -1, releaseCode: String? = null): Flow<Release> {
        return releaseItems.mapNotNull { it.findRelease(releaseId, releaseCode) }
    }

    fun observeFull(releaseId: Int = -1, releaseCode: String? = null): Flow<Release> {
        return combine(
            releases.mapNotNull { it.findRelease(releaseId, releaseCode) },
            episodesCheckerStorage.observeEpisodes(),
            checkerCombiner
        )
    }

    fun updateItemsCache(items: List<Release>) {
        releaseItems.update { releaseItems ->
            releaseItems.filterNot { release ->
                items.any {
                    check(release, it.id, it.code)
                }
            } + items
        }
    }

    fun updateFullCache(release: Release) {
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

    private fun <T : Release> List<T>.findRelease(id: Int, code: String?): T? = find {
        check(it, id, code)
    }

    private fun <T : Release> check(release: T, id: Int, code: String?): Boolean {
        val nullId = id.idOrNull()
        val releaseNullId = release.id.idOrNull()
        val releaseCode = release.code
        val foundById = releaseNullId != null && nullId != null && releaseNullId == nullId
        val foundByCode = releaseCode != null && code != null && releaseCode == code
        return foundById || foundByCode
    }

}