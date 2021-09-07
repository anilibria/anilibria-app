package ru.radiationx.data.entity.app.donation.other

import com.google.gson.annotations.SerializedName
import ru.radiationx.data.entity.app.donation.DonationLink

data class DonationInfraInfo(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("bt_telegram")
    val btTelegram: DonationLink?,
    @SerializedName("bt_cancel_text")
    val btCancelText: String
)
