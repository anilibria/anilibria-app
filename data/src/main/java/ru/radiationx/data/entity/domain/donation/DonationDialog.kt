package ru.radiationx.data.entity.domain.donation

data class DonationDialog(
    val tag: String,
    val content: List<DonationContentItem>,
    val cancelText: String?
)