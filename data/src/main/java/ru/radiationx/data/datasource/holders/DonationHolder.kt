package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.app.donation.DonationInfoResponse

interface DonationHolder {
    fun observe(): Flow<DonationInfoResponse>
    suspend fun get(): DonationInfoResponse
    suspend fun save(data: DonationInfoResponse)
    suspend fun delete()
}