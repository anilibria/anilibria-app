package ru.radiationx.data.entity.app.donation

import com.google.gson.annotations.SerializedName

data class DonationInfo<T>(
    @SerializedName("text")
    val text: String,
    @SerializedName("info")
    val info: T?
)
