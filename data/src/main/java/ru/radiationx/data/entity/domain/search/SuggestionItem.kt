package ru.radiationx.data.entity.domain.search

import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.release.BlockedInfo
import ru.radiationx.data.entity.domain.release.FavoriteInfo

data class SuggestionItem(
    val id: ReleaseId,
    val code: ReleaseCode,
    val names: List<String>,
    val poster: String?,
    val blockedInfo: BlockedInfo = BlockedInfo(false, null),
    val favoriteInfo: FavoriteInfo = FavoriteInfo(0, false),
)
