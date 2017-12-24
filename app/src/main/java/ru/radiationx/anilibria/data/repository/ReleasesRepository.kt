package ru.radiationx.anilibria.data.repository

import io.reactivex.Single
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.models.GenreItem
import ru.radiationx.anilibria.data.api.models.Paginated
import ru.radiationx.anilibria.data.api.models.ReleaseItem
import ru.radiationx.anilibria.data.api.models.SearchItem

/**
 * Created by radiationx on 17.12.17.
 */
class ReleasesRepository(private val api: Api) {

    fun getRelease(releaseId: Int): Single<ReleaseItem> = Single.defer {
        api.getRelease(releaseId)
    }

    fun getGenres(): Single<List<GenreItem>> = Single.defer {
        api.getGenres()
    }

    fun fastSearch(query: String): Single<List<SearchItem>> = Single.defer {
        api.fastSearch(query)
    }

    fun searchRelease(name: String, genre: String, page: Int): Single<Paginated<ArrayList<ReleaseItem>>> = Single.defer {
        api.searchRelease(name, genre, page)
    }

    fun getReleases(page: Int): Single<Paginated<ArrayList<ReleaseItem>>> = Single.defer {
        api.getReleases(page)
    }

}