package ru.radiationx.data.api.catalog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.api.catalog.models.CatalogFilterData
import ru.radiationx.data.api.catalog.models.CatalogFilterForm
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.shared.pagination.Paginated
import javax.inject.Inject

class CatalogRepository @Inject constructor(
    private val api: CatalogApiDataSource
) {

    suspend fun getFilterData(): CatalogFilterData {
        return withContext(Dispatchers.IO) {
            api.getFilterData()
        }
    }

    suspend fun getReleases(
        page: Int,
        form: CatalogFilterForm?
    ): Paginated<Release> {
        return withContext(Dispatchers.IO) {
            api.getReleases(page, form)
        }
    }
}