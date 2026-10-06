package ru.radiationx.anilibria.common

import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.release.BlockedInfo
import ru.radiationx.data.entity.domain.release.FavoriteInfo

data class LibriaCard(
    val title: String,
    val description: String,
    val image: String,
    val type: Type,
    val isBlocked: Boolean = false,
    val isFavorite: Boolean = false,
    val showFavoriteBadge: Boolean = true,
) : CardItem {

    override fun getId(): Int {
        return type.hashCode()
    }

    sealed class Type {
        data class Release(val releaseId: ReleaseId) : Type()
        data class Youtube(val link: String) : Type()
    }
}

internal fun LibriaCard.withReleaseStatus(
    blockedInfo: BlockedInfo,
    favoriteInfo: FavoriteInfo,
    showFavoriteBadge: Boolean = true,
): LibriaCard = copy(
    description = if (blockedInfo.isBlocked) {
        blockedInfo.reason?.trim()?.takeIf { it.isNotEmpty() } ?: "Релиз недоступен"
    } else {
        description
    },
    isBlocked = blockedInfo.isBlocked,
    isFavorite = favoriteInfo.isAdded,
    showFavoriteBadge = showFavoriteBadge,
)
