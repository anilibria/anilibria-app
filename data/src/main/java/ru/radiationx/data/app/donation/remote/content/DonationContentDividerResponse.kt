package ru.radiationx.data.app.donation.remote.content

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DonationContentDividerResponse(
    @Json(name = "height")
    val height: Int
)