package ru.radiationx.data.datasource.holders

interface DownloadsHolder {

    fun getDownloads(): List<Long>
    fun saveDownloads(items: List<Long>)
}