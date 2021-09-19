package ru.radiationx.data.entity.domain.donation

data class DonationInfo(
    val cardNewDonations: DonationCard?,
    val cardRelease: DonationCard?,
    val detailContent: List<DonationContentItem>,
    val detailContentData: DonationContentData
)
