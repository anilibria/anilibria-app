package ru.radiationx.data.api.collections

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.api.collections.models.CollectionReleaseId
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.api.collections.models.CollectionsFilterData
import ru.radiationx.data.api.collections.models.CollectionsFilterForm
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.shared.pagination.Paginated
import ru.radiationx.data.common.ReleaseId
import javax.inject.Inject

class CollectionsRepository @Inject constructor(
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

    suspend fun deleteRelease(releaseId: ReleaseId): Set<CollectionReleaseId> {
        return withContext(Dispatchers.IO) {
            api.deleteRelease(releaseId)
        }
    }

    suspend fun addRelease(releaseId: ReleaseId, type: CollectionType): Set<CollectionReleaseId> {
        return withContext(Dispatchers.IO) {
            api.addRelease(releaseId, type)
        }
    }
}