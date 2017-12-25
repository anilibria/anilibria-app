package ru.radiationx.anilibria.data.repository

import io.reactivex.Single
import ru.radiationx.anilibria.data.api.models.Paginated
import ru.radiationx.anilibria.data.api.models.release.GenreItem
import ru.radiationx.anilibria.data.api.models.release.ReleaseFull
import ru.radiationx.anilibria.data.api.models.release.ReleaseItem
import ru.radiationx.anilibria.data.api.modules.ReleaseApi

/**
 * Created by radiationx on 17.12.17.
 */
class ReleaseRepository(private val releaseApi: ReleaseApi) {

    fun getRelease(releaseId: Int): Single<ReleaseFull> = Single.defer {
        releaseApi.getRelease(releaseId)
    }

    fun getGenres(): Single<List<GenreItem>> = Single.defer {
        releaseApi.getGenres()
    }

    fun getReleases(page: Int): Single<Paginated<List<ReleaseItem>>> = Single.defer {
        releaseApi.getReleases(page)
    }

}
