package ru.radiationx.data.app.donation.models

data class DonationDialog(
    val tag: String,
    val content: List<DonationContentItem>,
    val cancelText: String?
)