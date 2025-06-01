package ru.radiationx.data.app.donation.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DonationInfoResponse(
    @Json(name = "cards")
    val cards: DonationCardsResponse,
    @Json(name = "detail")
    val detail: DonationDetailResponse
)
