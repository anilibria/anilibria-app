package ru.radiationx.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.holders.DonationHolder
import ru.radiationx.data.datasource.remote.api.DonationApi
import ru.radiationx.data.entity.domain.donation.DonationInfo
import ru.radiationx.data.entity.domain.donation.yoomoney.YooMoneyDialog
import ru.radiationx.data.entity.mapper.toDomain
import toothpick.InjectConstructor

@InjectConstructor
class DonationRepository(
    private val donationApi: DonationApi,
    private val donationHolder: DonationHolder,
    private val schedulers: SchedulersProvider
) {

    suspend fun requestUpdate() = donationApi
        .getDonationDetail()
        .also { donationHolder.save(it) }

    fun observerDonationInfo(): Flow<DonationInfo> = donationHolder
        .observe()
        .map { it.toDomain() }

    suspend fun createYooMoneyPayLink(
        amount: Int,
        type: String,
        form: YooMoneyDialog.YooMoneyForm
    ): String = donationApi.createYooMoneyPayLink(amount, type, form)
}