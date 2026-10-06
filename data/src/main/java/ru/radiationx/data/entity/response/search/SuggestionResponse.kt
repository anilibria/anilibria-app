package ru.radiationx.data.entity.response.search

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.radiationx.data.entity.response.release.BlockedInfoResponse
import ru.radiationx.data.entity.response.release.FavoriteInfoResponse

@JsonClass(generateAdapter = true)
data class SuggestionResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "code") val code: String,
    @Json(name = "names") val names: List<String>,
    @Json(name = "poster") val poster: String?,
    @Json(name = "blockedInfo") val blockedInfo: BlockedInfoResponse? = null,
    @Json(name = "favorite") val favorite: FavoriteInfoResponse? = null,
)
