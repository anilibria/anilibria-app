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
    val hasEpisodes: Boolean,
    val hasViewed: Boolean,
    val hasWebPlayer: Boolean
)

data class DetailsState(
    val loadingProgress: Boolean = false,
    val updateProgress: Boolean = false
)

class LibriaDetailsRow(id: Long, var details: LibriaDetails? = null, var state: DetailsState? = null) : Row(id, null)