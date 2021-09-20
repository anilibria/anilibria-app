package ru.radiationx.data.entity.app.donation

import com.google.gson.annotations.SerializedName
import ru.radiationx.data.entity.app.donation.content_data.DonationDialogResponse
import ru.radiationx.data.entity.app.donation.content_data.YooMoneyDialogResponse

data class DonationDetailResponse(
    @SerializedName("content")
    val content: List<DonationContentItemResponse>,
    @SerializedName("yoomoney_dialog")
    val yooMoneyDialog: YooMoneyDialogResponse?,
    @SerializedName("content_dialogs")
    val contentDialogs: List<DonationDialogResponse>
)
