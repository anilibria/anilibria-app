package ru.radiationx.data.entity.app.donation.other

import com.google.gson.annotations.SerializedName
import ru.radiationx.data.entity.app.donation.DonationLink

data class DonationJoinTeamInfo(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val desc: String,
    @SerializedName("voicer_notice")
    val voicerNotice: String?,
    @SerializedName("bt_voicer")
    val btVoicer: DonationLink?,
    @SerializedName("bt_telegram")
    val btTelegram: DonationLink?,
    @SerializedName("bt_cancel_text")
    val btCancelText: String
)