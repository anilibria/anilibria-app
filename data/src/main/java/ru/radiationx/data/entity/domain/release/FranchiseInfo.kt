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
) : Parcelable

@Parcelize
data class FranchiseRelease(
    val id: ReleaseId,
    val names: List<String>,
    val code: ReleaseCode,
) : Parcelable