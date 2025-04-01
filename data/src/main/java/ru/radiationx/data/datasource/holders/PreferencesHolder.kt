package ru.radiationx.data.datasource.holders

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.common.PlayerTransport
import javax.inject.Inject
import javax.inject.Singleton

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

    val playerAutoplay: AppPreference<Boolean>

    val notificationsAll: AppPreference<Boolean>

    val notificationsService: AppPreference<Boolean>

    /**
     * Список поддерживаемых скоростей воспроизведения.
     */
    val availableSpeeds: StateFlow<List<Float>>
}

/**
 * Реализация интерфейса. Тут Toothpick создаст PreferencesHolderImpl,
 * а все AppPreference<T> будут создаваться вручную в приватных полях.
 */
@Singleton
class PreferencesHolderImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : PreferencesHolder {

    // --- Приватные поля: AppPreference<T> ---

    private val _newDonationRemind = AppPreference(
        key = "newDonationRemind",
        sharedPreferences = sharedPreferences,
        get = { k -> getBoolean(k, false) },
        set = { k, v -> putBoolean(k, v) }
    )
    override val newDonationRemind: AppPreference<Boolean>
        get() = _newDonationRemind

    private val _releaseRemind = AppPreference(
        key = "releaseRemind",
        sharedPreferences = sharedPreferences,
        get = { k -> getBoolean(k, false) },
        set = { k, v -> putBoolean(k, v) }
    )
    override val releaseRemind: AppPreference<Boolean>
        get() = _releaseRemind

    private val _searchRemind = AppPreference(
        key = "searchRemind",
        sharedPreferences = sharedPreferences,
        get = { k -> getBoolean(k, false) },
        set = { k, v -> putBoolean(k, v) }
    )
    override val searchRemind: AppPreference<Boolean>
        get() = _searchRemind

    private val _episodesIsReverse = AppPreference(
        key = "episodesIsReverse",
        sharedPreferences = sharedPreferences,
        get = { k -> getBoolean(k, false) },
        set = { k, v -> putBoolean(k, v) }
    )
    override val episodesIsReverse: AppPreference<Boolean>
        get() = _episodesIsReverse

    private val _playerTransport = AppPreference(
        key = "playerTransport",
        sharedPreferences = sharedPreferences,
        get = { k ->
            val raw = getString(k, PlayerTransport.SYSTEM.name) ?: PlayerTransport.SYSTEM.name
            PlayerTransport.valueOf(raw)
        },
        set = { k, v -> putString(k, v.name) }
    )
    override val playerTransport: AppPreference<PlayerTransport>
        get() = _playerTransport

    private val _playerQuality = AppPreference(
        key = "playerQuality",
        sharedPreferences = sharedPreferences,
        get = { k ->
            val raw = getString(k, PlayerQuality.HD.name) ?: PlayerQuality.HD.name
            PlayerQuality.valueOf(raw)
        },
        set = { k, v -> putString(k, v.name) }
    )
    override val playerQuality: AppPreference<PlayerQuality>
        get() = _playerQuality

    private val _playSpeed = AppPreference(
        key = "playSpeed",
        sharedPreferences = sharedPreferences,
        get = { k -> getFloat(k, 1.0f) },
        set = { k, v -> putFloat(k, v) }
    )
    override val playSpeed: AppPreference<Float>
        get() = _playSpeed

    private val _playerSkips = AppPreference(
        key = "playerSkips",
        sharedPreferences = sharedPreferences,
        get = { k -> getBoolean(k, true) },
        set = { k, v -> putBoolean(k, v) }
    )
    override val playerSkips: AppPreference<Boolean>
        get() = _playerSkips

    private val _playerSkipsTimer = AppPreference(
        key = "playerSkipsTimer",
        sharedPreferences = sharedPreferences,
        get = { k -> getBoolean(k, true) },
        set = { k, v -> putBoolean(k, v) }
    )
    override val playerSkipsTimer: AppPreference<Boolean>
        get() = _playerSkipsTimer

    private val _playerInactiveTimer = AppPreference(
        key = "playerInactiveTimer",
        sharedPreferences = sharedPreferences,
        get = { k -> getBoolean(k, true) },
        set = { k, v -> putBoolean(k, v) }
    )
    override val playerInactiveTimer: AppPreference<Boolean>
        get() = _playerInactiveTimer

    private val _playerAutoplay = AppPreference(
        key = "playerAutoplay",
        sharedPreferences = sharedPreferences,
        get = { k -> getBoolean(k, true) },
        set = { k, v -> putBoolean(k, v) }
    )
    override val playerAutoplay: AppPreference<Boolean>
        get() = _playerAutoplay

    private val _notificationsAll = AppPreference(
        key = "notificationsAll",
        sharedPreferences = sharedPreferences,
        get = { k -> getBoolean(k, true) },
        set = { k, v -> putBoolean(k, v) }
    )
    override val notificationsAll: AppPreference<Boolean>
        get() = _notificationsAll

    private val _notificationsService = AppPreference(
        key = "notificationsService",
        sharedPreferences = sharedPreferences,
        get = { k -> getBoolean(k, true) },
        set = { k, v -> putBoolean(k, v) }
    )
    override val notificationsService: AppPreference<Boolean>
        get() = _notificationsService

    // Список скоростей воспроизведения (просто пример).
    private val _availableSpeeds = MutableStateFlow(listOf(0.75f, 1.0f, 1.25f, 1.5f))
    override val availableSpeeds: StateFlow<List<Float>>
        get() = _availableSpeeds
}

/**
 * Класс-обёртка над SharedPreferences для любого типа T.
 * Мы НЕ ставим @Inject, так как создаём вручную в PreferencesHolderImpl.
 */
class AppPreference<T>(
    private val key: String,
    private val sharedPreferences: SharedPreferences,
    private val get: SharedPreferences.(key: String) -> T,
    private val set: SharedPreferences.Editor.(key: String, value: T) -> Unit,
) : StateFlow<T> {

    private val _state by lazy { MutableStateFlow(value) }

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
        if (changedKey == this.key) {
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
            sharedPreferences.edit {
                set(key, value)
            }
        }

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        _state.collect(collector)
    }
}
