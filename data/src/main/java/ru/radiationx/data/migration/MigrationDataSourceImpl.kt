package ru.radiationx.data.migration

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.widget.Toast
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.AnalyticsErrorReporter
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class MigrationDataSourceImpl(
    private val context: Context,
    private val defaultPreferences: SharedPreferences,
    private val sharedBuildConfig: SharedBuildConfig,
    private val migrationExecutor: MigrationExecutor,
    private val errorReporter: AnalyticsErrorReporter
) : MigrationDataSource {

    companion object {
        private const val PREF_KEY = "app.versions.history"
        private const val INITIAL_VERSION = 0
        private const val ANALYTIC_GROUP = "migration"
    }

    override fun getHistory(): List<Int> {
        return defaultPreferences
            .getString(PREF_KEY, "")
            ?.split(";")
            ?.filter { it.isNotBlank() }
            ?.map { it.toInt() }
            ?: emptyList()
    }

    override fun update() {
        try {
            val history = getHistory()
            val currentVersion = sharedBuildConfig.versionCode
            val lastVersion = history.lastOrNull() ?: INITIAL_VERSION
            val disorder = checkIsDisordered(history)

            if (lastVersion < currentVersion) {
                if (lastVersion > INITIAL_VERSION) {
                    migrationExecutor.execute(currentVersion, lastVersion, history)
                }
                val newHistory = history + currentVersion
                saveHistory(newHistory)
            }
            if (disorder) {
                val errMsg =
                    "AniLibria: Нарушение порядка версий, программа может работать не стабильно!"
                errorReporter.report(ANALYTIC_GROUP, errMsg)
                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
            }
        } catch (ex: Throwable) {
            Timber.e(ex)
            val errMsg = "Сбой при проверке локальной версии."
            errorReporter.report(ANALYTIC_GROUP, errMsg, ex)
            val uiErr = "$errMsg\nПрограмма может работать не стабильно! Переустановите программу."
            Toast.makeText(context, uiErr, Toast.LENGTH_LONG).show()
        }
    }

    private fun checkIsDisordered(history: List<Int>): Boolean {
        var prevVersion = 0
        history.forEach {
            if (it < prevVersion) {
                return true
            }
            prevVersion = it
        }
        return false
    }

    private fun saveHistory(history: List<Int>) {
        defaultPreferences
            .edit()
            .putString(PREF_KEY, TextUtils.join(";", history.map { it.toString() }))
            .apply()
    }
}