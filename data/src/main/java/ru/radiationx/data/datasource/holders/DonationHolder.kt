package ru.radiationx.data.datasource.holders

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.entity.app.donation.DonationDetail

interface DonationHolder {
    fun observe(): Observable<DonationDetail>
    fun get(): Single<DonationDetail>
    fun save(data: DonationDetail): Completable
    fun delete(): Completable
}