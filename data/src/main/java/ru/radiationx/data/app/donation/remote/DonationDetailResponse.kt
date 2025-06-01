package ru.radiationx.data.app.donation.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.radiationx.data.app.donation.remote.content_data.DonationDialogResponse
import ru.radiationx.data.app.donation.remote.content_data.YooMoneyDialogResponse

@JsonClass(generateAdapter = true)
data class DonationDetailResponse(
    @Json(name = "content")
    val content: List<DonationContentItemResponse>,
    @Json(name = "yoomoney_dialog")
    val yooMoneyDialog: YooMoneyDialogResponse?,
    @Json(name = "content_dialogs")
    val contentDialogs: List<DonationDialogResponse>
)
