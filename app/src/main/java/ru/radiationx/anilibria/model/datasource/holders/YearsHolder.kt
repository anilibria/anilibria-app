package ru.radiationx.anilibria.model.datasource.holders

import io.reactivex.Observable
import ru.radiationx.data.entity.app.release.YearItem

interface YearsHolder {
    fun observeYears(): Observable<MutableList<YearItem>>
    fun saveYears(genres: List<YearItem>)
    fun getYears(): List<YearItem>
}