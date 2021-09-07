package ru.radiationx.anilibria.presentation.donation.infra

import ru.radiationx.data.entity.app.donation.donate.DonationYooMoneyInfo

data class DonationYooMoneyState(
    val data: DonationYooMoneyInfo? = null,
    val selectedAmount: Int? = null,
    val amountType: AmountType = AmountType.PRESET,
    val customAmount: Int? = null,
    val selectedPaymentTypeId: String? = null,
    val acceptEnabled: Boolean = false
) {
    enum class AmountType {
        PRESET, CUSTOM
    }
}
