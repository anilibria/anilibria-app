package ru.radiationx.anilibria.presentation.donation.infra

import ru.radiationx.data.entity.app.donation.donate.DonationYooMoneyInfo

data class DonationYooMoneyState(
    val data: DonationYooMoneyInfo,
    val selectedAmount: Int?,
    val amountType: AmountType,
    val selectedType: String?
) {
    enum class AmountType {
        PRESET, CUSTOM
    }
}
