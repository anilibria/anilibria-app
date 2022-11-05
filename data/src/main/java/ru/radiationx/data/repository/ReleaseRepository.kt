package ru.radiationx.data.repository

import ru.radiationx.data.datasource.remote.api.ReleaseApi
import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.app.release.RandomRelease
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import javax.inject.Inject

/**
 * Created by radiationx on 17.12.17.
 */
class ReleaseRepository @Inject constructor(
    private val releaseApi: ReleaseApi,
    private val updateMiddleware: ReleaseUpdateMiddleware
) {

    suspend fun getRandomRelease(): RandomRelease = releaseApi.getRandomRelease()

    suspend fun getRelease(releaseId: Int): ReleaseItem = releaseApi
        .getRelease(releaseId)
        .also { updateMiddleware.handle(it) }

    suspend fun getRelease(releaseIdName: String): ReleaseItem = releaseApi
        .getRelease(releaseIdName)
        .also { updateMiddleware.handle(it) }

    suspend fun getReleasesById(ids: List<Int>): List<ReleaseItem> = releaseApi
        .getReleasesByIds(ids)
        .also { updateMiddleware.handle(it) }

    suspend fun getReleases(page: Int): Paginated<List<ReleaseItem>> = releaseApi
        .getReleases(page)
        .also { updateMiddleware.handle(it.data) }
}
