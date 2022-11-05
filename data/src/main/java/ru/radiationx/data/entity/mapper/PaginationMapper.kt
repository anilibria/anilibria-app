package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.app.Paginated
import ru.radiationx.data.entity.response.PaginatedResponse

fun <T, R> PaginatedResponse<T>.toDomain(block: (T) -> R): Paginated<R> = Paginated(
    data = data.map(block),
    page = meta.page,
    allPages = meta.allPages,
    perPage = meta.perPage,
    allItems = meta.allItems
)