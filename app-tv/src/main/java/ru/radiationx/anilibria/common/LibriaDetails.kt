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
    val hasFullHd: Boolean
)

class LibriaDetailsRow(val details: LibriaDetails) : Row()