package ru.radiationx.data.migration

interface MigrationDataSource {
    fun getHistory(): List<Int>
    fun update()
}