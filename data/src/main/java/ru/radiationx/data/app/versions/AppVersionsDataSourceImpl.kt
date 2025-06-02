package ru.radiationx.data.app.versions

import android.content.SharedPreferences
import androidx.core.content.edit
import ru.radiationx.data.SharedBuildConfig
import timber.log.Timber
import javax.inject.Inject

class AppVersionsDataSourceImpl @Inject constructor(
    private val defaultPreferences: SharedPreferences,
    private val sharedBuildConfig: SharedBuildConfig
) : AppVersionsDataSource {

    companion object {
        private const val PREF_KEY = "app.versions.history"
        private const val INITIAL_VERSION = 0
        private const val SEPARATOR = ";"
    }

    override fun getHistory(): List<Int> {
        return try {
            defaultPreferences
                .getString(PREF_KEY, "")
                ?.split(SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?.map { it.toInt() }
                ?: emptyList()
        } catch (ex: Exception) {
            Timber.e(ex)
            emptyList()
        }
    }

    override fun update() {
        val history = getHistory()
        val currentVersion = sharedBuildConfig.versionCode
        val lastVersion = history.lastOrNull() ?: INITIAL_VERSION

        if (lastVersion < currentVersion) {
            val newHistory = history + currentVersion
            saveHistory(newHistory)
        }
    }

    private fun saveHistory(history: List<Int>) {
        defaultPreferences.edit {
            val historyStr = history.joinToString(separator = SEPARATOR) { it.toString() }
            putString(PREF_KEY, historyStr)
        }
    }
}