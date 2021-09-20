package ru.radiationx.data.entity.app.donation.content

import com.google.gson.annotations.SerializedName

data class DonationContentHeaderResponse(
    @SerializedName("text")
    val text: String
)