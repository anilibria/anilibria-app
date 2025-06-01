package ru.radiationx.data.api.releases.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.common.GenreId

@Parcelize
data class ReleaseGenre(
    val id: GenreId,
    val name: String
) : Parcelable
