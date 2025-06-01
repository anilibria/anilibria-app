package ru.radiationx.anilibria.model

import ru.radiationx.data.common.ReleaseCode
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.data.common.Url

data class SuggestionItemState(
    val id: ReleaseId,
    val code: ReleaseCode,
    val title: String,
    val poster: Url.Relative?,
    val matchRanges: List<IntRange>
)