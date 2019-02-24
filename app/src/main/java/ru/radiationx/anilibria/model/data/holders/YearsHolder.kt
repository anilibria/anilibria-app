package ru.radiationx.anilibria.model.data.holders

import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.release.YearItem

interface YearsHolder {
    fun observeYears(): Observable<MutableList<YearItem>>
    fun saveYears(genres: List<YearItem>)
    fun getYears(): List<YearItem>
}