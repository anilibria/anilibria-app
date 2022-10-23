package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.DownloadsHolder
import toothpick.InjectConstructor

@InjectConstructor
class DownloadsStorage(
    @DataPreferences private val sharedPreferences: SharedPreferences
) : DownloadsHolder {

    companion object {
        private const val KEY_DOWNLOADS = "data.download_ids"
    }

    private val currentDownloads by lazy {
        val result = mutableListOf<Long>()
        sharedPreferences.getString(KEY_DOWNLOADS, null)
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.map { it.toLong() }
            ?.also {
                result.addAll(it)
            }
        result
    }

    override fun getDownloads(): List<Long> = currentDownloads.toList()

    override fun saveDownloads(items: List<Long>) {
        sharedPreferences.edit().putString(KEY_DOWNLOADS, items.joinToString(",")).apply()
        currentDownloads.clear()
        currentDownloads.addAll(items)
    }
}