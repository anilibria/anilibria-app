package ru.radiationx.anilibria.ui.activities.player.controllers

import android.app.AppOpsManager
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.util.Consumer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.radiationx.shared.ktx.android.immutableFlag
import timber.log.Timber


class PictureInPictureController(
    private val activity: ComponentActivity,
) {

    private companion object {
        const val ACTION_REMOTE_CONTROL = "action.remote.control"
        const val EXTRA_REMOTE_CONTROL = "extra.remote.control"
    }

    private val modeListener = Consumer<PictureInPictureModeChangedInfo> { mode ->
        _state.update { it.copy(active = mode.isInPictureInPictureMode) }
    }

    private val actionsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null || intent.action != ACTION_REMOTE_CONTROL) {
                return
            }
            val extraCode = intent.getIntExtra(EXTRA_REMOTE_CONTROL, Int.MIN_VALUE)
            _paramsState.value.actions.find { it.code == extraCode }?.also { action ->
                actionsListener?.invoke(action)
            }
        }
    }

    private val _paramsState = MutableStateFlow(ParamsState())

    private var _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    var actionsListener: ((Action) -> Unit)? = null

    fun init() {
        updateState()
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                updateState()
                activity.addOnPictureInPictureModeChangedListener(modeListener)
                registerActionsReceiver()
            }

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                updateState()
            }

            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                updateState()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                activity.removeOnPictureInPictureModeChangedListener(modeListener)
                unregisterActionsReceiver()
            }
        })

        _paramsState.onEach { paramsState ->
            _state.update {
                it.copy(isValidParams = paramsState.isValid)
            }
            setParams(paramsState)
        }.launchIn(activity.lifecycleScope)
    }

    fun enter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && _state.value.canEnter) {
            try {
                val params = _paramsState.value.toParams() ?: return
                val result = activity.enterPictureInPictureMode(params)
                _state.update { it.copy(active = result) }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    fun updateParams(block: (ParamsState) -> ParamsState) {
        _paramsState.update(block)
    }

    private fun setParams(paramsState: ParamsState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val params = paramsState.toParams() ?: return
                activity.setPictureInPictureParams(params)
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    private fun updateState() {
        _state.update {
            it.copy(
                supports = isPipSupports(),
                active = isInPictureInPictureMode()
            )
        }
    }

    private fun registerActionsReceiver() {
        val filter = IntentFilter(ACTION_REMOTE_CONTROL)
        val flags = ContextCompat.RECEIVER_EXPORTED
        ContextCompat.registerReceiver(activity, actionsReceiver, filter, flags)
    }

    private fun unregisterActionsReceiver() {
        try {
            activity.unregisterReceiver(actionsReceiver)
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ParamsState.toParams(): PictureInPictureParams? {
        if (!isRectValid) {
            Timber.tag("ParamsStateValidation").d("invalid source hint rect $sourceHintRect")
            return null
        }
        if (!isAspectRatioValid) {
            Timber.tag("ParamsStateValidation").d("invalid aspect ratio $aspectRatio")
            return null
        }

        val builder = PictureInPictureParams.Builder()
        val remoteActions = actions
            .takeLast(activity.maxNumPictureInPictureActions)
            .map { it.toRemoteAction() }

        builder.setSourceRectHint(sourceHintRect)
        builder.setAspectRatio(aspectRatio)
        builder.setActions(remoteActions)
        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Action.toRemoteAction(): RemoteAction {
        val actionEnabled = isEnabled
        val icon = Icon.createWithResource(activity, icRes)
        val intent = Intent(ACTION_REMOTE_CONTROL).putExtra(EXTRA_REMOTE_CONTROL, code)
        val pendingIntent = PendingIntent.getBroadcast(activity, code, intent, immutableFlag())
        val remoteAction = RemoteAction(icon, title, title, pendingIntent).apply {
            isEnabled = actionEnabled
        }
        return remoteAction
    }

    private fun isInPictureInPictureMode(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.isInPictureInPictureMode
        } else {
            false
        }

    private fun isPipSupports(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false
        }
        val hasFeature =
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        val hasPermission = hasPipPermission()
        return hasFeature && hasPermission
    }

    @Suppress("DEPRECATION")
    private fun hasPipPermission(): Boolean {
        val appOps = activity.getSystemService<AppOpsManager>() ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        }
        val op = AppOpsManager.OPSTR_PICTURE_IN_PICTURE
        val pid = android.os.Process.myUid()
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(op, pid, activity.packageName)
        } else {
            appOps.checkOpNoThrow(op, pid, activity.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    data class State(
        val supports: Boolean = false,
        val active: Boolean = false,
        val isValidParams: Boolean = false,
    ) {
        val canEnter = supports && isValidParams
    }

    data class ParamsState(
        val sourceHintRect: Rect = Rect(),
        val aspectRatio: Rational = Rational(0, 0),
        val actions: List<Action> = emptyList(),
    ) {

        val isRectValid = sourceHintRect.let {
            it.width() != 0 && it.width() != 0
        }

        val isAspectRatioValid = aspectRatio.let {
            !it.isNaN && !it.isInfinite && !it.isZero
        }

        val isValid = isRectValid && isAspectRatioValid
    }

    data class Action(
        val code: Int,
        val title: String,
        @DrawableRes val icRes: Int,
        val isEnabled: Boolean = true,
    )
}