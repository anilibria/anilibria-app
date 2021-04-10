package ru.radiationx.data.repository

import io.reactivex.Observable
import ru.radiationx.data.entity.app.donation.DonationDetail

class DonationRepository {
    fun observerDonationDetail(): Observable<DonationDetail> = Observable.empty()
}