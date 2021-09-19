package ru.radiationx.data.entity.app.donation.content

import com.google.gson.annotations.SerializedName

data class DonationContentButtonResponse(
    @SerializedName("tag")
    val tag: String?,
    @SerializedName("text")
    val text: String,
    @SerializedName("link")
    val link: String?,
    @SerializedName("brand")
    val brand: String?,
    @SerializedName("icon")
    val icon: String?
)