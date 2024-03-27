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


class OrientationController(
    private val activity: ComponentActivity,
) {

    private val configurationListener = Consumer<Configuration> { config ->
        _state.update { it.copy(actualOrientation = config.toOrientation()) }
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
            .map { it.resultOrientation }
            .distinctUntilChanged()
            .onEach {
                activity.requestedOrientation = it.toActivityOrientation()
            }
            .launchIn(activity.lifecycleScope)
    }

    fun setOrientation(orientation: Orientation) {
        _state.update { it.copy(requestedOrientation = orientation) }
    }

    fun updateOrientation(block: (Orientation?) -> Orientation?) {
        _state.update { it.copy(requestedOrientation = block.invoke(it.requestedOrientation)) }
    }

    fun setUiLock(state: Boolean) {
        val lockOrientation = if (state) {
            activity.resources.configuration.toOrientation()
        } else {
            null
        }
        _state.update { it.copy(lockOrientation = lockOrientation) }
    }

    private fun updateState() {
        val actualOrientation = activity.resources.configuration.toOrientation()
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
                actualOrientation = actualOrientation,
                accelerometerEnabled = accelerometerEnabled
            )
        }
    }

    private fun Configuration.toOrientation(): Orientation? {
        return when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
            Configuration.ORIENTATION_PORTRAIT -> Orientation.PORTRAIT
            else -> null
        }
    }

    private fun Orientation?.toActivityOrientation(): Int {
        return when (this) {
            Orientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            Orientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            null -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    data class State(
        val actualOrientation: Orientation? = null,
        val requestedOrientation: Orientation? = null,
        val lockOrientation: Orientation? = null,
        val isMultiWindow: Boolean = false,
        val accelerometerEnabled: Boolean = false,
    ) {
        val available = !isMultiWindow && !accelerometerEnabled

        val resultOrientation: Orientation? = when {
            lockOrientation != null -> lockOrientation
            available -> requestedOrientation
            else -> null
        }
    }

    enum class Orientation {
        LANDSCAPE,
        PORTRAIT
    }
}