package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.app.release.YearItem

interface YearsHolder {
    fun observeYears(): Flow<List<YearItem>>
    fun saveYears(years: List<YearItem>)
    fun getYears(): List<YearItem>
}