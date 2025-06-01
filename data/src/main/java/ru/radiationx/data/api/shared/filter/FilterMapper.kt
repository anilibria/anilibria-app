package ru.radiationx.data.api.shared.filter

import anilibria.api.shared.filter.FilterAgeRatingResponse
import anilibria.api.shared.filter.FilterGenreResponse
import anilibria.api.shared.filter.FilterProductionStatusResponse
import anilibria.api.shared.filter.FilterPublishStatusResponse
import anilibria.api.shared.filter.FilterReleaseTypeResponse
import anilibria.api.shared.filter.FilterSeasonResponse
import anilibria.api.shared.filter.FilterSortingResponse
import ru.radiationx.data.common.GenreId

fun FilterAgeRatingResponse.toDomain(): FilterItem.Value {
    return FilterItem.Value(value, label, description)
}

fun FilterReleaseTypeResponse.toDomain(): FilterItem.Value {
    return FilterItem.Value(value, description, null)
}

fun FilterGenreResponse.toDomain(): FilterItem.Genre {
    return FilterItem.Genre(GenreId(id), name)
}

fun FilterProductionStatusResponse.toDomain(): FilterItem.Value {
    return FilterItem.Value(value, description, null)
}

fun FilterPublishStatusResponse.toDomain(): FilterItem.Value {
    return FilterItem.Value(value, description, null)
}

fun FilterSeasonResponse.toDomain(): FilterItem.Value {
    return FilterItem.Value(value, description, value)
}

fun FilterSortingResponse.toDomain(): FilterItem.Value {
    return FilterItem.Value(value, label, description)
}

fun Int.toDomainFilterYear(): FilterItem.Year {
    return FilterItem.Year(this, this.toString())
}



fun Iterable<FormItem>.toListRequest(): List<String> {
    return map { it.toRequest() }
}

fun Iterable<FormItem>.toStringRequest(): String {
    return toListRequest().joinToString(",")
}

fun FormItem.toRequest(): String {
    return when (this) {
        is FormItem.Genre -> id.id.toString()
        is FormItem.Value -> value
        is FormItem.Year -> year.toString()
    }
}