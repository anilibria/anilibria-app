package ru.radiationx.data.entity.mapper

import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.domain.search.SuggestionItem
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.response.search.SuggestionResponse

fun SuggestionResponse.toDomain(
    apiConfig: ApiConfig,
) = SuggestionItem(
    id = ReleaseId(id),
    code = ReleaseCode(code),
    names = names,
    poster = poster?.appendBaseUrl(apiConfig.baseImagesUrl)
)

