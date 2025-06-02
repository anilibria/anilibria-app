package ru.radiationx.data.api.genres.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.common.GenreId
import ru.radiationx.data.common.Url

@Parcelize
data class Genre(
    val id: GenreId,
    val name: String,
    val totalReleases: Int,
    val image: Url.Path?
) : Parcelable
