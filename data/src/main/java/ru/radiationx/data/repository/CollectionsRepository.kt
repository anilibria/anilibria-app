package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.apinext.datasources.CollectionsApiDataSource
import ru.radiationx.data.apinext.models.CollectionReleaseId
import ru.radiationx.data.apinext.models.enums.CollectionType
import ru.radiationx.data.apinext.models.filters.CollectionsFilterData
import ru.radiationx.data.apinext.models.filters.CollectionsFilterForm
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
import toothpick.InjectConstructor

@InjectConstructor
class CollectionsRepository(
    private val api: CollectionsApiDataSource
) {

    suspend fun getFilterData(): CollectionsFilterData {
        return withContext(Dispatchers.IO) {
            api.getFilterData()
        }
    }

    suspend fun getReleases(
        type: CollectionType,
        page: Int,
        form: CollectionsFilterForm?
    ): Paginated<Release> {
        return withContext(Dispatchers.IO) {
            api.getReleases(type, page, form)
        }
    }

    suspend fun getReleaseIds(): Set<CollectionReleaseId> {
        return withContext(Dispatchers.IO) {
            api.getReleaseIds()
        }
    }

    suspend fun deleteRelease(releaseId: ReleaseId) {
        withContext(Dispatchers.IO) {
            api.deleteRelease(releaseId)
        }
    }

    suspend fun addRelease(releaseId: ReleaseId, type: CollectionType) {
        withContext(Dispatchers.IO) {
            api.addRelease(releaseId, type)
        }
    }
}