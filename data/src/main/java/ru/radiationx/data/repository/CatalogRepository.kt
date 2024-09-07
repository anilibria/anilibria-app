package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.apinext.datasources.CatalogApiDataSource
import ru.radiationx.data.apinext.models.filters.CatalogFilterData
import ru.radiationx.data.apinext.models.filters.CatalogFilterForm
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.Release
import toothpick.InjectConstructor

@InjectConstructor
class CatalogRepository(
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