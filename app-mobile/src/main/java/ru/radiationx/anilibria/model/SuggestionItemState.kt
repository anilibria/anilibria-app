package ru.radiationx.anilibria.model

data class SuggestionItemState(
    val id: Int,
    val title: String,
    val poster: String,
    val matchRanges: List<IntRange>
)