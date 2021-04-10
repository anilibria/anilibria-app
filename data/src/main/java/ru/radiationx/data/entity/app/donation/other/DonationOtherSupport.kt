package ru.radiationx.data.entity.app.donation.other

import com.google.gson.annotations.SerializedName
import ru.radiationx.data.entity.app.donation.DonationInfo

data class DonationOtherSupport(
    @SerializedName("title")
    val title: String,
    @SerializedName("bt_join_team")
    val btJoinTeam: DonationInfo<DonationJoinTeamInfo>?,
    @SerializedName("bt_infra")
    val btInfra: DonationInfo<DonationInfraInfo>?
)
