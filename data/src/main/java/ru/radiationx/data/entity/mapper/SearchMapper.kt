package ru.radiationx.data.entity.mapper

import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.app.release.YearItem
import ru.radiationx.data.entity.app.search.SuggestionItem
import ru.radiationx.data.entity.response.search.SuggestionResponse
import ru.radiationx.data.system.ApiUtils

fun SuggestionResponse.toDomain(
    apiUtils: ApiUtils,
    apiConfig: ApiConfig
) = SuggestionItem(
    id = id,
    code = code,
    names = names.map {
        apiUtils.escapeHtml(it).toString()
    },
    poster = poster?.appendBaseUrl(apiConfig.baseImagesUrl)
)

fun String.toYearItem(): YearItem = YearItem(
    title = this,
    value = this
)

fun String.toGenreItem(): YearItem = YearItem(
    title = this.capitalize(),
    value = this
)