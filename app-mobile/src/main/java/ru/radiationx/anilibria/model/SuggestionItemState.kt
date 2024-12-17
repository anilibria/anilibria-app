package ru.radiationx.anilibria.model

import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId

data class SuggestionItemState(
    val id: ReleaseId,
    val code: ReleaseCode,
    val title: String,
    val poster: String,
    val matchRanges: List<IntRange>
)