package ru.radiationx.data.entity.app.donation.content_data

import com.google.gson.annotations.SerializedName
import ru.radiationx.data.entity.app.donation.DonationContentItemResponse

data class DonationDialogResponse(
    @SerializedName("tag")
    val tag: String,
    @SerializedName("content")
    val content: List<DonationContentItemResponse>,
    @SerializedName("cancel_text")
    val cancelText: String?
)