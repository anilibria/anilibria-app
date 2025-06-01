package ru.radiationx.data.api.favorites.mapper

import anilibria.api.collections.models.FavoritesFiltersRequest
import anilibria.api.favorites.models.FavoritesRequest
import ru.radiationx.data.api.favorites.models.FavoritesFilterForm
import ru.radiationx.data.api.shared.filter.toListRequest
import ru.radiationx.data.api.shared.filter.toStringRequest

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