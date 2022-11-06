package ru.radiationx.anilibria.model

import ru.radiationx.data.entity.domain.types.ReleaseId

data class SuggestionItemState(
    val id: ReleaseId,
    val title: String,
    val poster: String,
    val matchRanges: List<IntRange>
)