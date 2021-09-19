package ru.radiationx.data.entity.app.donation

import com.google.gson.annotations.SerializedName

data class DonationDetailResponse(
    @SerializedName("content")
    val content: List<DonationContentItemResponse>,
    @SerializedName("content_data")
    val contentData: DonationContentDataResponse?
)
