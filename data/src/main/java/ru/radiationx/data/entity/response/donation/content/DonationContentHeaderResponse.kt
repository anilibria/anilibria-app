package ru.radiationx.data.entity.response.donation.content

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DonationContentHeaderResponse(
    @Json(name = "text")
    val text: String
)