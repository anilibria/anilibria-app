package ru.radiationx.data.di.providers

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Provider

class PreferencesProvider @Inject constructor(
    private val context: Context,
) : Provider<SharedPreferences> {
    @Suppress("DEPRECATION")
    override fun get(): SharedPreferences {
        // for strict-mode pass
        return runBlocking {
            withContext(Dispatchers.IO) {
                PreferenceManager.getDefaultSharedPreferences(context)
            }
        }
    }
}

class DataPreferencesProvider @Inject constructor(
    private val context: Context,
) : Provider<SharedPreferences> {
    override fun get(): SharedPreferences {
        // for strict-mode pass
        return runBlocking {
            withContext(Dispatchers.IO) {
                context.getSharedPreferences(
                    "${context.packageName}_datastorage",
                    Context.MODE_PRIVATE
                )
            }
        }
    }
}