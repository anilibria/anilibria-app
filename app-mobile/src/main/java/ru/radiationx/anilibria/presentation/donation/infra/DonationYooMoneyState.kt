package ru.radiationx.anilibria.presentation.donation.infra

import ru.radiationx.data.entity.domain.donation.yoomoney.YooMoneyDialog

data class DonationYooMoneyState(
    val data: YooMoneyDialog? = null,
    val selectedAmount: Int? = null,
    val amountType: AmountType = AmountType.PRESET,
    val customAmount: Int? = null,
    val selectedPaymentTypeId: String? = null,
    val acceptEnabled: Boolean = false,
    val sending: Boolean = false
) {
    enum class AmountType {
        PRESET, CUSTOM
    }
}
