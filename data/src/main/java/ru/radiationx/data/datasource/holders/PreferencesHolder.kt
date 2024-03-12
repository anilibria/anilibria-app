package ru.radiationx.data.datasource.holders

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.common.PlayerTransport

/**
 * Created by radiationx on 03.02.18.
 */
interface PreferencesHolder {

    val newDonationRemind: AppPreference<Boolean>

    val releaseRemind: AppPreference<Boolean>

    val searchRemind: AppPreference<Boolean>

    val episodesIsReverse: AppPreference<Boolean>

    val playerTransport: AppPreference<PlayerTransport>

    val playerQuality: AppPreference<PlayerQuality>

    val playSpeed: AppPreference<Float>

    val playerSkips: AppPreference<Boolean>

    val playerSkipsTimer: AppPreference<Boolean>

    val playerInactiveTimer: AppPreference<Boolean>

    val notificationsAll: AppPreference<Boolean>

    val notificationsService: AppPreference<Boolean>

}

class AppPreference<T>(
    private val key: String,
    private val sharedPreferences: SharedPreferences,
    private val get: SharedPreferences.(key: String) -> T,
    private val set: SharedPreferences.Editor.(key: String, value: T) -> Unit,
) : StateFlow<T> {

    private val _state by lazy { MutableStateFlow(value) }

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == this.key) {
            _state.value = value
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override val replayCache: List<T>
        get() = _state.replayCache

    override var value: T
        get() = get(sharedPreferences, key)
        set(value) {
            sharedPreferences.edit { set(key, value) }
        }

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        _state.collect(collector)
    }
}