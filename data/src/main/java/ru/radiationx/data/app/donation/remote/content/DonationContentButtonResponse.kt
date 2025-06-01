package ru.radiationx.data.app.donation.remote.content

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DonationContentButtonResponse(
    @Json(name = "tag")
    val tag: String?,
    @Json(name = "text")
    val text: String,
    @Json(name = "link")
    val link: String?,
    @Json(name = "brand")
    val brand: String?,
    @Json(name = "icon")
    val icon: String?
)