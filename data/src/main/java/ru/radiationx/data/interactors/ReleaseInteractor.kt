package ru.radiationx.data.interactors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.release.RandomRelease
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
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
                val episodeAccess = episodeAccesses.firstOrNull { it.id == episode.id }
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

    private val sharedRequests = SharedRequests<RequestKey, Release>()

    suspend fun getRandomRelease(): RandomRelease = releaseRepository.getRandomRelease()

    private suspend fun loadRelease(releaseId: ReleaseId): Release {
        return releaseRepository.getRelease(releaseId).also(::updateFullCache)
    }

    private suspend fun loadRelease(releaseCode: ReleaseCode): Release {
        return releaseRepository.getRelease(releaseCode).also(::updateFullCache)
    }

    suspend fun loadRelease(
        releaseId: ReleaseId? = null,
        releaseCode: ReleaseCode? = null
    ): Release {
        val key = RequestKey(releaseId, releaseCode)
        return sharedRequests.request(key) {
            when {
                releaseId != null -> loadRelease(releaseId)
                releaseCode != null -> loadRelease(releaseCode)
                else -> throw Exception("Unknown id=$releaseId or code=$releaseCode")
            }
        }
    }

    fun getItem(releaseId: ReleaseId? = null, releaseCode: ReleaseCode? = null): Release? {
        return releaseItems.value.findRelease(releaseId, releaseCode)
    }

    suspend fun getFull(releaseId: ReleaseId? = null, releaseCode: ReleaseCode? = null): Release? {
        return observeFull(releaseId, releaseCode).firstOrNull()
    }

    fun observeItem(releaseId: ReleaseId? = null, releaseCode: ReleaseCode? = null): Flow<Release> {
        return releaseItems.mapNotNull { it.findRelease(releaseId, releaseCode) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeFull(releaseId: ReleaseId? = null, releaseCode: ReleaseCode? = null): Flow<Release> {
        return flow {
            emit(updateIfNotExists(releaseId, releaseCode))
        }.flatMapLatest {
            combine(
                releases.mapNotNull { it.findRelease(releaseId, releaseCode) },
                episodesCheckerStorage.observeEpisodes(),
                checkerCombiner
            )
        }
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
    suspend fun putEpisode(episode: EpisodeAccess) = episodesCheckerStorage.putEpisode(episode)

    suspend fun putEpisodes(episodes: List<EpisodeAccess>) =
        episodesCheckerStorage.putAllEpisode(episodes)

    suspend fun getEpisodes(releaseId: ReleaseId) = episodesCheckerStorage.getEpisodes(releaseId)

    suspend fun resetEpisodesHistory(releaseId: ReleaseId) {
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


    private suspend fun updateIfNotExists(
        releaseId: ReleaseId? = null,
        releaseCode: ReleaseCode? = null
    ) {
        val release = releases.value.findRelease(releaseId, releaseCode)
        if (release != null) {
            return
        }
        runCatching {
            loadRelease(releaseId, releaseCode)
        }
    }

    private fun List<Release>.findRelease(id: ReleaseId?, code: ReleaseCode?): Release? = find {
        check(it, id, code)
    }

    private fun check(release: Release, id: ReleaseId?, code: ReleaseCode?): Boolean {
        val foundById = id != null && release.id == id
        val foundByCode = code != null && release.code == code
        return foundById || foundByCode
    }

    data class RequestKey(
        val id: ReleaseId?,
        val code: ReleaseCode?
    )

}