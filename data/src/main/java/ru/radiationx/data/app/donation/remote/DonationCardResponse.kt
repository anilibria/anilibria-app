package ru.radiationx.data.app.donation.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DonationCardResponse(
    @Json(name = "title")
    val title: String,
    @Json(name = "subtitle")
    val subtitle: String?
)
