package ru.radiationx.data.entity.domain.donation

data class DonationDialog(
    val content: List<DonationContentItem>,
    val cancelText: String?
)