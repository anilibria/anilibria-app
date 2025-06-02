package ru.radiationx.data.app.versions

interface AppVersionsDataSource {
    fun getHistory(): List<Int>
    fun update()
}