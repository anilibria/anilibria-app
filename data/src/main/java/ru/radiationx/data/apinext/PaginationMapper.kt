package ru.radiationx.data.apinext

import anilibria.api.shared.PaginationResponse
import ru.radiationx.data.entity.domain.Paginated


fun <T, R> PaginationResponse<T>.toDomain(block: (T) -> R): Paginated<R> = Paginated(
    data = data.map(block),
    page = meta.pagination.currentPage,
    allPages = meta.pagination.totalPages,
    perPage = meta.pagination.perPage,
    allItems = meta.pagination.total
)