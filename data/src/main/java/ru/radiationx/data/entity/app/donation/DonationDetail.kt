package ru.radiationx.data.entity.app.donation

import com.google.gson.annotations.SerializedName
import ru.radiationx.data.entity.app.donation.donate.DonationDonateSupport
import ru.radiationx.data.entity.app.donation.other.DonationOtherSupport

data class DonationDetail(
    @SerializedName("title")
    val title: String,
    @SerializedName("good")
    val good: DonationDesc?,
    @SerializedName("bad")
    val bad: DonationDesc?,
    @SerializedName("footer_text")
    val footerText: String?,
    @SerializedName("donate_support")
    val donateSupport: DonationDonateSupport?,
    @SerializedName("other_support")
    val otherSupport: DonationOtherSupport?
)
