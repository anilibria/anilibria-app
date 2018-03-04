package ru.radiationx.anilibria.model.data.storage

import android.content.SharedPreferences
import ru.radiationx.anilibria.model.data.holders.PreferencesHolder

/**
 * Created by radiationx on 03.02.18.
 */
class PreferencesStorage(
        private val sharedPreferences: SharedPreferences
) : PreferencesHolder {

    companion object {
        private const val RELEASE_REMIND_KEY = "release_remind"
        private const val SEARCH_REMIND_KEY = "search_remind"
        private const val EPISODES_IS_REVERSE_KEY = "episodes_is_reverse"
        private const val QUALITY_KEY = "quality"
    }

    override fun getReleaseRemind(): Boolean {
        return sharedPreferences.getBoolean(RELEASE_REMIND_KEY, true)
    }

    override fun setReleaseRemind(state: Boolean) {
        sharedPreferences.edit().putBoolean(RELEASE_REMIND_KEY, state).apply()
    }

    override fun getSearchRemind(): Boolean {
        return sharedPreferences.getBoolean(SEARCH_REMIND_KEY, true)
    }

    override fun setSearchRemind(state: Boolean) {
        sharedPreferences.edit().putBoolean(SEARCH_REMIND_KEY, state).apply()
    }

    override fun getEpisodesIsReverse(): Boolean {
        return sharedPreferences.getBoolean(EPISODES_IS_REVERSE_KEY, false)
    }

    override fun getQuality(): Int {
        return sharedPreferences.getInt(QUALITY_KEY, PreferencesHolder.QUALITY_NO)
    }

    override fun setQuality(value: Int) {
        sharedPreferences.edit().putInt(QUALITY_KEY, value).apply()
    }
}