package ru.radiationx.anilibria.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.ReleaseInteractor
import timber.log.Timber

internal fun ReleaseInteractor.observePosterWatched(
    releaseId: ReleaseId,
    series: String?,
): Flow<Boolean> = posterWatchedState(observeAccesses(releaseId)) {
    flow {
        // List responses omit the playlist. Resolve it only for releases with viewed marks.
        val cached = if (series == null) loadRelease(releaseId) else getFull(releaseId)
        // A newer list range invalidates the cached playlist. Quick search has no range.
        if (series != null && cached?.series != series) loadRelease(releaseId)
        emitAll(observeFull(releaseId).map { release -> release.episodes.map { it.id } })
    }.catch {
        Timber.w(it, "Unable to resolve watched poster for release %s", releaseId.id)
        emit(emptyList())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun posterWatchedState(
    accesses: Flow<List<EpisodeAccess>>,
    episodes: () -> Flow<List<EpisodeId>>,
): Flow<Boolean> = accesses
    .map { list -> list.any { it.isViewed } }
    .distinctUntilChanged()
    .flatMapLatest { hasViewed ->
        if (!hasViewed) flowOf(false) else combine(episodes(), accesses) { ids, marks ->
            val viewedIds = marks.filter { it.isViewed }.mapTo(mutableSetOf()) { it.id }
            ids.isNotEmpty() && ids.all { it in viewedIds }
        }
    }
    .distinctUntilChanged()
