package ru.radiationx.data.api.genres

import anilibria.api.genres.GenresApi
import ru.radiationx.data.api.genres.mapper.toDomain
import ru.radiationx.data.api.genres.models.Genre
import ru.radiationx.data.api.releases.mapper.toDomain
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.shared.pagination.Paginated
import ru.radiationx.data.api.shared.pagination.toDomain
import ru.radiationx.data.common.GenreId
import javax.inject.Inject

class GenresApiDataSource @Inject constructor(
    private val api: GenresApi
) {

    suspend fun getGenres(): List<Genre> {
        return api
            .getGenres()
            .map { it.toDomain() }
    }

    suspend fun getGenre(id: GenreId): Genre {
        return api
            .getGenre(id.id)
            .toDomain()
    }

    suspend fun getRandomGenres(limit: Int?): List<Genre> {
        return api
            .getRandomGenres(limit)
            .map { it.toDomain() }
    }

    suspend fun getGenreReleases(
        id: GenreId,
        page: Int?,
        limit: Int?
    ): Paginated<Release> {
        return api
            .getGenreReleases(id.id, page, limit)
            .toDomain { it.toDomain() }
    }
}