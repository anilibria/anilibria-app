package ru.radiationx.data.entity.app.donation.donate

import com.google.gson.annotations.SerializedName
import ru.radiationx.data.entity.app.donation.DonationInfo
import ru.radiationx.data.entity.app.donation.DonationLink

data class DonationDonateSupport(
    @SerializedName("title")
    val title: String,
    @SerializedName("bt_patreon")
    val btPatreon: DonationLink?,
    @SerializedName("bt_yoomoney")
    val btYooMoney: DonationInfo<DonationYooMoneyInfo>?,
    @SerializedName("bt_donationalerts")
    val btDonationAlerts: DonationLink?
)
