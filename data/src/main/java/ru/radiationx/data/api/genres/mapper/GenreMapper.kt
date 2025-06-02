package ru.radiationx.data.api.genres.mapper

import anilibria.api.genres.models.GenreResponse
import ru.radiationx.data.api.genres.models.Genre
import ru.radiationx.data.common.GenreId
import ru.radiationx.data.common.toPathUrl

fun GenreResponse.toDomain():Genre{
    return Genre(
        id = GenreId(id),
        name = name,
        totalReleases = totalReleases,
        image = image.preview?.toPathUrl()
    )
}