package ru.radiationx.data.app.donation

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.app.donation.remote.DonationInfoResponse

interface DonationHolder {
    fun observe(): Flow<DonationInfoResponse>
    suspend fun get(): DonationInfoResponse
    suspend fun save(data: DonationInfoResponse)
}