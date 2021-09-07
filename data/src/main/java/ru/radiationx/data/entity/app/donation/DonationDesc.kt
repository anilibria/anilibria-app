package ru.radiationx.data.entity.app.donation

import com.google.gson.annotations.SerializedName

data class DonationDesc(
    @SerializedName("title")
    val title: String,
    @SerializedName("desc")
    val desc: String
)