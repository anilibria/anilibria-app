package ru.radiationx.anilibria.ui.activities.player.controllers

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.app.MultiWindowModeChangedInfo
import androidx.core.util.Consumer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update


class FullScreenController(
    private val activity: ComponentActivity,
) {

    private val configurationListener = Consumer<Configuration> { config ->
        _state.update { it.copy(actualFullScreen = config.orientation == Configuration.ORIENTATION_LANDSCAPE) }
    }

    private val accelerometerObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            updateState()
        }
    }

    private val multiwindowListener = Consumer<MultiWindowModeChangedInfo> { mode ->
        _state.update { it.copy(isMultiWindow = mode.isInMultiWindowMode) }
    }

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun init() {
        updateState()
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                updateState()
                activity.addOnConfigurationChangedListener(configurationListener)
                activity.addOnMultiWindowModeChangedListener(multiwindowListener)
                activity.contentResolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                    true,
                    accelerometerObserver
                )
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                activity.addOnConfigurationChangedListener(configurationListener)
                activity.addOnMultiWindowModeChangedListener(multiwindowListener)
                activity.contentResolver.unregisterContentObserver(accelerometerObserver)
            }
        })
        _state
            .map { it.requestedFullscreen && !it.accelerometerEnabled }
            .distinctUntilChanged()
            .onEach {
                activity.requestedOrientation = if (it) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
            .launchIn(activity.lifecycleScope)
    }

    fun setFullscreen(state: Boolean) {
        _state.update { it.copy(requestedFullscreen = state) }
    }

    fun toggleFullscreen() {
        _state.update { it.copy(requestedFullscreen = !it.requestedFullscreen) }
    }

    private fun updateState() {
        val isFullScreen =
            activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val isMultiWindow = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.isInMultiWindowMode
        } else {
            false
        }
        val accelerometerEnabled = Settings.System.getInt(
            activity.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION
        ) == 1
        _state.update {
            it.copy(
                isMultiWindow = isMultiWindow,
                actualFullScreen = isFullScreen,
                accelerometerEnabled = accelerometerEnabled
            )
        }
    }

    data class State(
        val isMultiWindow: Boolean = false,
        val actualFullScreen: Boolean = false,
        val requestedFullscreen: Boolean = false,
        val accelerometerEnabled: Boolean = false,
    ) {
        val available = !isMultiWindow && !accelerometerEnabled
    }
}