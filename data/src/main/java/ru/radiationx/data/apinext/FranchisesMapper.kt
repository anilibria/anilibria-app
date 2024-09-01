package ru.radiationx.data.apinext

import anilibria.api.franchises.models.FranchiseReleaseResponse
import anilibria.api.franchises.models.FranchiseResponse
import ru.radiationx.data.entity.domain.release.Franchise
import ru.radiationx.data.entity.domain.release.FranchiseInfo
import ru.radiationx.data.entity.domain.release.FranchiseRelease
import ru.radiationx.data.entity.domain.types.FranchiseId

fun FranchiseResponse.toDomain(): Franchise {
    return Franchise(
        info = toInfoDomain(),
        releases = franchiseReleases?.map { it.toDomain() }.orEmpty()
    )
}

fun FranchiseResponse.toInfoDomain(): FranchiseInfo {
    return FranchiseInfo(
        id = FranchiseId(id),
        name = name,
        nameEnglish = nameEnglish,
        rating = rating,
        lastYear = lastYear,
        firstYear = firstYear,
        totalReleases = totalReleases,
        totalEpisodes = totalEpisodes,
        totalDuration = totalDuration,
        totalDurationInSeconds = totalDurationInSeconds,
        image = image.src
    )
}

fun FranchiseReleaseResponse.toDomain(): FranchiseRelease {
    val domainRelease = release.toDomain()
    return FranchiseRelease(
        id = id,
        releaseId = domainRelease.id,
        franchiseId = FranchiseId(franchiseId),
        release = domainRelease
    )
}