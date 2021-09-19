package ru.radiationx.data.entity.app.donation

import com.google.gson.annotations.SerializedName

data class DonationCardResponse(
    @SerializedName("title")
    val title: String,
    @SerializedName("subtitle")
    val subtitle: String?
)
