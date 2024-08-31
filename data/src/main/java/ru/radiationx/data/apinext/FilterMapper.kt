package ru.radiationx.data.apinext

import anilibria.api.shared.filter.FilterAgeRatingResponse
import anilibria.api.shared.filter.FilterCollectionTypeResponse
import anilibria.api.shared.filter.FilterGenreResponse
import anilibria.api.shared.filter.FilterProductionsStatusResponse
import anilibria.api.shared.filter.FilterPublishStatusResponse
import anilibria.api.shared.filter.FilterSeasonResponse
import anilibria.api.shared.filter.FilterSortingResponse
import ru.radiationx.data.apinext.models.filter.FilterAgeRating
import ru.radiationx.data.apinext.models.filter.FilterCollectionType
import ru.radiationx.data.apinext.models.filter.FilterGenre
import ru.radiationx.data.apinext.models.filter.FilterProductionsStatus
import ru.radiationx.data.apinext.models.filter.FilterPublishStatus
import ru.radiationx.data.apinext.models.filter.FilterSeason
import ru.radiationx.data.apinext.models.filter.FilterSorting
import ru.radiationx.data.apinext.models.filter.FilterYear

fun FilterAgeRatingResponse.toDomain(): FilterAgeRating {
    return FilterAgeRating(value, label, description)
}

fun FilterCollectionTypeResponse.toDomain(): FilterCollectionType {
    return FilterCollectionType(value, description)
}

fun FilterGenreResponse.toDomain(): FilterGenre {
    return FilterGenre(id, name)
}

fun FilterProductionsStatusResponse.toDomain(): FilterProductionsStatus {
    return FilterProductionsStatus(value, description)
}

fun FilterPublishStatusResponse.toDomain(): FilterPublishStatus {
    return FilterPublishStatus(value, description)
}

fun FilterSeasonResponse.toDomain(): FilterSeason {
    return FilterSeason(value, description)
}

fun FilterSortingResponse.toDomain(): FilterSorting {
    return FilterSorting(value, label, description)
}

fun Int.toDomainFilterYear(): FilterYear {
    return FilterYear(this, this.toString())
}
