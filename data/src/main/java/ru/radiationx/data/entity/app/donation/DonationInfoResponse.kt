package ru.radiationx.data.entity.app.donation

import com.google.gson.annotations.SerializedName

data class DonationInfoResponse(
    @SerializedName("cards")
    val cards: DonationCardsResponse,
    @SerializedName("detail")
    val detail: DonationDetailResponse
)
