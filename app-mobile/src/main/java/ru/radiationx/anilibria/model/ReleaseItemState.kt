package ru.radiationx.anilibria.model

import ru.radiationx.data.common.ReleaseId
import ru.radiationx.data.common.Url

data class ReleaseItemState(
    val id: ReleaseId,
    val title: String,
    val description: String,
    val posterUrl: Url.Relative?,
    val isNew: Boolean
)