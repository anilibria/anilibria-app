package ru.radiationx.data.datasource.holders

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.entity.app.donation.DonationDetailResponse

interface DonationHolder {
    fun observe(): Observable<DonationDetailResponse>
    fun get(): Single<DonationDetailResponse>
    fun save(data: DonationDetailResponse): Completable
    fun delete(): Completable
}