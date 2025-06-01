package ru.radiationx.anilibria.ui.fragments.donation.yoomoney

import ru.radiationx.data.app.donation.models.YooMoneyDialog

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
