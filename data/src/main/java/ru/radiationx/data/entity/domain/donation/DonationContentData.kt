package ru.radiationx.data.entity.domain.donation

import ru.radiationx.data.entity.domain.donation.yoomoney.YooMoneyDialog

data class DonationContentData(
    val joinTeamDialog: DonationDialog?,
    val infraDialog: DonationDialog?,
    val yooMoneyDialog: YooMoneyDialog?
)