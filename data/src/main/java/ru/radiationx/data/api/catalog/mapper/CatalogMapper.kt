package ru.radiationx.data.api.catalog.mapper

import anilibria.api.catalog.models.CatalogFiltersRequest
import anilibria.api.catalog.models.CatalogRequest
import ru.radiationx.data.api.catalog.models.CatalogFilterForm
import ru.radiationx.data.api.shared.filter.toListRequest

fun CatalogFilterForm?.toRequest(
    page: Int,
): CatalogRequest {
    return CatalogRequest(
        page = page,
        limit = null,
        filters = this?.toFilterRequest()
    )
}

fun CatalogFilterForm.toFilterRequest(): CatalogFiltersRequest {
    return CatalogFiltersRequest(
        genres = genres.toListRequest(),
        types = types.toListRequest(),
        seasons = seasons.toListRequest(),
        years = yearsRange?.let {
            CatalogFiltersRequest.Years(
                from = it.first.year,
                to = it.second.year
            )
        },
        search = query,
        sorting = sorting?.value,
        ageRatings = ageRatings.toListRequest(),
        publishStatuses = publishStatuses.toListRequest(),
        productionStatuses = productionStatuses.toListRequest()
    )
}