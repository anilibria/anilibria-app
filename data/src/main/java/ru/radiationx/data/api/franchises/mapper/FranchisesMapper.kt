package ru.radiationx.data.api.franchises.mapper

import anilibria.api.franchises.models.FranchiseResponse
import ru.radiationx.data.api.franchises.models.Franchise
import ru.radiationx.data.api.franchises.models.FranchiseFull
import ru.radiationx.data.api.releases.mapper.toDomain
import ru.radiationx.data.common.FranchiseId
import ru.radiationx.data.common.toPathUrl

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
        image = image.preview?.toPathUrl()
    )
}

fun FranchiseResponse.toDomainFull(): FranchiseFull {
    return FranchiseFull(
        info = toDomain(),
        releases = franchiseReleases?.map { it.release.toDomain() }.orEmpty()
    )
}