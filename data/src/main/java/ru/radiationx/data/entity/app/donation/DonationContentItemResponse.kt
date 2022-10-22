package ru.radiationx.data.entity.app.donation

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.radiationx.data.entity.app.donation.content.*

@JsonClass(generateAdapter = true)
data class DonationContentItemResponse(
    @Json(name = "type")
    val type: String?,
    @Json(name = "header")
    val header: DonationContentHeaderResponse?,
    @Json(name = "caption")
    val caption: DonationContentCaptionResponse?,
    @Json(name = "section")
    val section: DonationContentSectionResponse?,
    @Json(name = "button")
    val button: DonationContentButtonResponse?,
    @Json(name = "divider")
    val divider: DonationContentDividerResponse?
)