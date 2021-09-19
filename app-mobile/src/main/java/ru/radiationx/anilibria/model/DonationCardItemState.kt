package ru.radiationx.anilibria.model

data class DonationCardItemState(
    val tag: String,
    val title: String,
    val subtitle: String?,
    val canClose: Boolean
)