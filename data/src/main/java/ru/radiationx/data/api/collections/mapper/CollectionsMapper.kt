package ru.radiationx.data.api.collections.mapper

import anilibria.api.collections.models.CollectionsFiltersRequest
import anilibria.api.collections.models.CollectionsRequest
import anilibria.api.shared.CollectionReleaseIdNetwork
import ru.radiationx.data.api.collections.models.CollectionReleaseId
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.api.collections.models.CollectionsFilterForm
import ru.radiationx.data.api.shared.filter.toListRequest
import ru.radiationx.data.api.shared.filter.toStringRequest
import ru.radiationx.data.common.ReleaseId

fun CollectionsFilterForm?.toRequest(
    type: CollectionType,
    page: Int,
): CollectionsRequest {
    return CollectionsRequest(
        typeOfCollection = type.toRequest(),
        page = page,
        limit = null,
        filters = this?.toFilterRequest()
    )
}

fun CollectionsFilterForm.toFilterRequest(): CollectionsFiltersRequest {
    return CollectionsFiltersRequest(
        genres = genres.toStringRequest(),
        types = types.toListRequest(),
        years = years.toStringRequest(),
        search = query,
        ageRatings = ageRatings.toListRequest()
    )
}

fun CollectionReleaseIdNetwork.toDomain(): CollectionReleaseId {
    return CollectionReleaseId(
        id = ReleaseId(releaseId),
        type = typeOfCollection.toCollectionType()
    )
}

fun String.toCollectionType(): CollectionType {
    return when (this) {
        "PLANNED" -> CollectionType.Planned
        "WATCHING" -> CollectionType.Watching
        "WATCHED" -> CollectionType.Watched
        "POSTPONED" -> CollectionType.Postponed
        "ABANDONED" -> CollectionType.Abandoned
        else -> CollectionType.Unknown(this)
    }
}

fun CollectionType.toRequest(): String {
    return when (this) {
        CollectionType.Planned -> "PLANNED"
        CollectionType.Watching -> "WATCHING"
        CollectionType.Watched -> "WATCHED"
        CollectionType.Postponed -> "POSTPONED"
        CollectionType.Abandoned -> "ABANDONED"
        is CollectionType.Unknown -> raw
    }
}