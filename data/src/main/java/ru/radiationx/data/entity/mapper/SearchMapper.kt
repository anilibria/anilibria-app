package ru.radiationx.data.entity.mapper

import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.entity.domain.release.YearItem
import ru.radiationx.data.entity.domain.search.SuggestionItem
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.response.search.SuggestionResponse
import ru.radiationx.data.system.ApiUtils

fun SuggestionResponse.toDomain(
    apiUtils: ApiUtils,
    apiConfig: ApiConfig
) = SuggestionItem(
    id = ReleaseId(id),
    code = ReleaseCode(code),
    names = names.map {
        apiUtils.escapeHtml(it).toString()
    },
    poster = poster?.appendBaseUrl(apiConfig.baseImagesUrl)
)

fun String.toYearItem(): YearItem = YearItem(
    title = this,
    value = this
)

fun String.toGenreItem(): GenreItem = GenreItem(
    title = this.capitalize(),
    value = this
)