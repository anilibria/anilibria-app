package ru.radiationx.anilibria.screen.details

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.radiationx.data.entity.domain.release.Release

internal fun Flow<Release>.withFavoriteChanges(changes: Flow<Boolean?>): Flow<Release> =
    combine(this, changes) { release, isFavorite ->
        // A successful local action takes priority over stale list/full release responses.
        if (isFavorite == null) release else release.copy(
            favoriteInfo = release.favoriteInfo.copy(isAdded = isFavorite)
        )
    }
