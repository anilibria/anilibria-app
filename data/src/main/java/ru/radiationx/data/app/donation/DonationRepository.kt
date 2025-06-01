package ru.radiationx.data.app.donation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.radiationx.data.app.donation.mapper.toDomain
import ru.radiationx.data.app.donation.models.DonationInfo
import ru.radiationx.data.app.donation.models.YooMoneyDialog
import ru.radiationx.data.common.Url
import ru.radiationx.data.common.toAbsoluteUrl
import javax.inject.Inject

class DonationRepository @Inject constructor(
    private val donationApi: DonationApiDataSource,
    private val donationHolder: DonationHolder,
) {

    suspend fun requestUpdate() = withContext(Dispatchers.IO) {
        donationApi
            .getDonationDetail()
            .also { donationHolder.save(it) }
    }

    fun observerDonationInfo(): Flow<DonationInfo> = donationHolder
        .observe()
        .map { it.toDomain() }
        .flowOn(Dispatchers.IO)

    suspend fun createYooMoneyPayLink(
        amount: Int,
        type: String,
        form: YooMoneyDialog.YooMoneyForm
    ): Url.Absolute = withContext(Dispatchers.IO) {
        donationApi.createYooMoneyPayLink(amount, type, form).toAbsoluteUrl()
    }
}