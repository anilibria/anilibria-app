package ru.radiationx.data.entity.app.donation

import com.google.gson.annotations.SerializedName

data class DonationLink(
    @SerializedName("text")
    val text: String,
    @SerializedName("link")
    val link: String?
)
