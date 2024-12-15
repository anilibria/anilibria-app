package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toNavFromParam
import ru.radiationx.data.analytics.features.extensions.toParam
import ru.radiationx.data.analytics.features.model.AnalyticsDonationAmountType
import ru.radiationx.data.analytics.features.model.AnalyticsDonationPaymentType
import javax.inject.Inject

class DonationYooMoneyAnalytics @Inject constructor(
    private val sender: AnalyticsSender
) {

    private companion object {
        const val PARAM_AMOUNT = "amount"
        const val PARAM_AMOUNT_TYPE = "amount_type"
        const val PARAM_PAYMENT_TYPE = "payment_type"
    }

    fun open(from: String) {
        sender.send(
            AnalyticsConstants.donation_yoomoney_open,
            from.toNavFromParam()
        )
    }

    fun helpClick() {
        sender.send(AnalyticsConstants.donation_yoomoney_help_click)
    }

    fun acceptClick(
        amount: Int?,
        amountType: AnalyticsDonationAmountType?,
        paymentType: AnalyticsDonationPaymentType?
    ) {
        sender.send(
            AnalyticsConstants.donation_yoomoney_accept_click,
            amount.toParam(PARAM_AMOUNT),
            amountType.toParam(PARAM_AMOUNT_TYPE),
            paymentType.toParam(PARAM_PAYMENT_TYPE)
        )
    }


}