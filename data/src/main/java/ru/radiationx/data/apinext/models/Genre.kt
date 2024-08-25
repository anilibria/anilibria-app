package ru.radiationx.data.apinext.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.domain.types.GenreId

@Parcelize
data class Genre(
    val id: GenreId,
    val name: String
) : Parcelable
