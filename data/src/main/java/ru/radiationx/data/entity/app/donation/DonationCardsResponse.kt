package ru.radiationx.data.entity.app.donation

import com.google.gson.annotations.SerializedName

data class DonationCardsResponse(
    @SerializedName("new_donations")
    val newDonations: DonationCardResponse?,
    @SerializedName("release")
    val release: DonationCardResponse?
)