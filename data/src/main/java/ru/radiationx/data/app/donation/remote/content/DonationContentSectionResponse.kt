package ru.radiationx.data.app.donation.remote.content

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DonationContentSectionResponse(
    @Json(name = "title")
    val title: String?,
    @Json(name = "subtitle")
    val subtitle: String?
)
