package ru.radiationx.data.entity.app.donation.content

import com.google.gson.annotations.SerializedName

data class DonationContentSectionResponse(
    @SerializedName("title")
    val title: String?,
    @SerializedName("subtitle")
    val subtitle: String?
)
