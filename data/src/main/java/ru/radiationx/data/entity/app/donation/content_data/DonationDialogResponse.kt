package ru.radiationx.data.entity.app.donation.content_data

import com.google.gson.annotations.SerializedName
import ru.radiationx.data.entity.app.donation.DonationContentItemResponse

data class DonationDialogResponse(
    @SerializedName("content")
    val content: List<DonationContentItemResponse>,
    @SerializedName("cancelText")
    val cancelText: String?
)