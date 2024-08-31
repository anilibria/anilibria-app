package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId

@Parcelize
data class Franchise(
    val info: FranchiseInfo,
    val releases: List<FranchiseRelease>,
) : Parcelable

@Parcelize
data class FranchiseInfo(
    val id: String,
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
data class FranchiseRelease(
    val id: String,
    val releaseId: ReleaseId,
    val franchiseId:String,
    // todo API2 update usage
    //val names: List<String>,
    // todo API2 update usage
    //val code: ReleaseCode,
    val release: Release
) : Parcelable