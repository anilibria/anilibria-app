package ru.radiationx.data.api.franchises.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.api.releases.models.Release

@Parcelize
data class FranchiseFull(
    val info: Franchise,
    val releases: List<Release>,
) : Parcelable

fun List<FranchiseFull>.getAllReleases(): List<Release> {
    return fold(mutableListOf()) { acc, franchiseFull ->
        acc.addAll(franchiseFull.releases)
        acc
    }
}