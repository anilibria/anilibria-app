package ru.radiationx.data.datasource.holders

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.entity.domain.release.YearItem

interface YearsHolder {
    fun observeYears(): Flow<List<YearItem>>
    suspend fun saveYears(years: List<YearItem>)
    suspend fun getYears(): List<YearItem>
}