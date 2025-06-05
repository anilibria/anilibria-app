package ru.radiationx.anilibria.model

import ru.radiationx.data.common.ReleaseId
import ru.radiationx.data.common.Url

data class SuggestionItemState(
    val id: ReleaseId,
    val title: String,
    val poster: Url.Path?,
    val matchRanges: List<IntRange>
)