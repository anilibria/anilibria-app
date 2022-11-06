package ru.radiationx.data.entity.domain.search

import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId

data class SuggestionItem(
    val id: ReleaseId,
    val code: ReleaseCode,
    val names: List<String>,
    val poster: String?
)