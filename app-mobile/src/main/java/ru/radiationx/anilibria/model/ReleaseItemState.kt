package ru.radiationx.anilibria.model

import ru.radiationx.data.entity.common.Url
import ru.radiationx.data.entity.domain.types.ReleaseId

data class ReleaseItemState(
    val id: ReleaseId,
    val title: String,
    val description: String,
    val posterUrl: Url.Relative?,
    val isNew: Boolean
)