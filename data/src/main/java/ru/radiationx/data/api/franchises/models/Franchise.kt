package ru.radiationx.data.api.franchises.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.common.FranchiseId
import ru.radiationx.data.common.Url

@Parcelize
data class Franchise(
    val id: FranchiseId,
    val name: String,
    val nameEnglish: String,
    val rating: Double?,
    val lastYear: Int,
    val firstYear: Int,
    val totalReleases: Int,
    val totalEpisodes: Int,
    val totalDuration: String,
    val totalDurationInSeconds: Int,
    val image: Url.Relative?,
) : Parcelable