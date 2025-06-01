package ru.radiationx.shared.ktx.android

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferenceFlow<T>(
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