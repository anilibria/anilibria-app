package ru.radiationx.data.entity.app.donation.content_data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.radiationx.data.entity.app.donation.DonationContentItemResponse

@JsonClass(generateAdapter = true)
data class DonationDialogResponse(
    @Json(name = "tag")
    val tag: String,
    @Json(name = "content")
    val content: List<DonationContentItemResponse>,
    @Json(name = "cancel_text")
    val cancelText: String?
)