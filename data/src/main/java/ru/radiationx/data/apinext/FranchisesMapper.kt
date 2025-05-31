package ru.radiationx.data.apinext

import anilibria.api.franchises.models.FranchiseResponse
import ru.radiationx.data.entity.domain.release.Franchise
import ru.radiationx.data.entity.domain.release.FranchiseFull
import ru.radiationx.data.entity.domain.types.FranchiseId

fun FranchiseResponse.toDomain(): Franchise {
    return Franchise(
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
        image = image.preview?.toRelativeUrl()
    )
}

fun FranchiseResponse.toDomainFull(): FranchiseFull {
    return FranchiseFull(
        info = toDomain(),
        releases = franchiseReleases?.map { it.release.toDomain() }.orEmpty()
    )
}