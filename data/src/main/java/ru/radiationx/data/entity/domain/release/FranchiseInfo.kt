package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.domain.types.FranchiseId
import ru.radiationx.data.entity.domain.types.ReleaseId

@Parcelize
data class Franchise(
    val id: FranchiseId,
    val name: String,
    val nameEnglish: String,
    val rating: Double,
    val lastYear: Int,
    val firstYear: Int,
    val totalReleases: Int,
    val totalEpisodes: Int,
    val totalDuration: String,
    val totalDurationInSeconds: Int,
    val image: String,
) : Parcelable

@Parcelize
data class FranchiseFull(
    val info: Franchise,
    val releases: List<Release>,
) : Parcelable
