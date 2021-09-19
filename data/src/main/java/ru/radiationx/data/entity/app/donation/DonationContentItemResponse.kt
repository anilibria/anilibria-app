package ru.radiationx.data.entity.app.donation

import com.google.gson.annotations.SerializedName
import ru.radiationx.data.entity.app.donation.content.*

data class DonationContentItemResponse(
    @SerializedName("type")
    val type: String,
    @SerializedName("header")
    val header: DonationContentHeaderResponse?,
    @SerializedName("caption")
    val caption: DonationContentCaptionResponse?,
    @SerializedName("section")
    val section: DonationContentSectionResponse?,
    @SerializedName("button")
    val button: DonationContentButtonResponse?,
    @SerializedName("divider")
    val divider: DonationContentDividerResponse?
)