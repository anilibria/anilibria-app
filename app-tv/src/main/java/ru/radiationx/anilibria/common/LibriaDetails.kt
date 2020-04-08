package ru.radiationx.anilibria.common

import androidx.leanback.widget.Row

data class LibriaDetails(
    val id: Int,
    val titleRu: String,
    val titleEn: String,
    val extra: String,
    val description: String,
    val announce: String,
    val image: String,
    val favoriteCount: String,
    val hasFullHd: Boolean,
    val isFavorite: Boolean,
    val hasViewed: Boolean,
    val hasWebPlayer: Boolean
)

class LibriaDetailsRow(id: Long, var details: LibriaDetails? = null) : Row(id, null)