package ru.radiationx.anilibria.model

data class ReleaseItemState(
    val id: Int,
    val title: String,
    val description: String,
    val posterUrl: String,
    val isNew: Boolean
)