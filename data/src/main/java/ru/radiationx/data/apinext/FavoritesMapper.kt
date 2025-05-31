package ru.radiationx.data.apinext

import anilibria.api.collections.models.FavoritesFiltersRequest
import anilibria.api.favorites.models.FavoritesRequest
import ru.radiationx.data.apinext.models.filters.FavoritesFilterForm

fun FavoritesFilterForm?.toRequest(
    page: Int,
): FavoritesRequest {
    return FavoritesRequest(
        page = page,
        limit = null,
        filters = this?.toFilterRequest()
    )
}

fun FavoritesFilterForm.toFilterRequest(): FavoritesFiltersRequest {
    return FavoritesFiltersRequest(
        genres = genres.toStringRequest(),
        types = types.toListRequest(),
        years = years.toStringRequest(),
        search = query,
        sorting = sorting?.value,
        ageRatings = ageRatings.toListRequest()
    )
}