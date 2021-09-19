package ru.radiationx.data.entity.app.donation

import com.google.gson.annotations.SerializedName
import ru.radiationx.data.entity.app.donation.content_data.DonationDialogResponse
import ru.radiationx.data.entity.app.donation.content_data.YooMoneyDialogResponse

data class DonationContentDataResponse(
    @SerializedName("join_team_dialog")
    val joinTeamDialog: DonationDialogResponse?,
    @SerializedName("infra_dialog")
    val infraDialog: DonationDialogResponse?,
    @SerializedName("infra_dialog")
    val yooMoneyDialog: YooMoneyDialogResponse?
)