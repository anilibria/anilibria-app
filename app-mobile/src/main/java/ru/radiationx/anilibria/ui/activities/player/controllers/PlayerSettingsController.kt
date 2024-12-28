package ru.radiationx.anilibria.ui.activities.player.controllers

import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemPlayerSpeedsBinding
import ru.radiationx.anilibria.utils.view.attachedCoroutineScope
import ru.radiationx.data.entity.common.PlayerQuality
import envoy.DiffItem
import taiwa.TaiwaAction
import taiwa.TaiwaAnchor
import taiwa.bottomsheet.nestedBottomSheetTaiwa
import envoy.ext.viewBindingEnvoy
import java.math.BigDecimal

class PlayerSettingsController(
    private val activity: ComponentActivity,
) {

    private val taiwa by activity.nestedBottomSheetTaiwa()

    private val qualityAnchor = TaiwaAnchor.Id("quality")
    private val speedAnchor = TaiwaAnchor.Id("speed")
    private val skipsAnchor = TaiwaAnchor.Id("skips")


    var onQualitySelected: ((PlayerQuality) -> Unit)? = null
    var onSpeedSelected: ((Float) -> Unit)? = null
    var onSkipsSelected: ((Boolean) -> Unit)? = null
    var onSkipsTimerSelected: ((Boolean) -> Unit)? = null
    var onInactiveTimerSelected: ((Boolean) -> Unit)? = null
    var onAutoplaySelected: ((Boolean) -> Unit)? = null

    init {
        taiwa.addDelegate(speedSelectorEnvoy {
            onSpeedSelected?.invoke(it)
        })
    }

    fun show() {
        taiwa.show()
    }

    fun setState(state: PlayerSettingsState) {
        taiwa.setContent {
            header {
                title("Настройки плеера")
                canClose()
            }
            items {
                item {
                    title(SettingItem.Quality.toTitle())
                    icon(state.currentQuality.toIcRes())
                    value(state.currentQuality.toValue())
                    action(TaiwaAction.Anchor(qualityAnchor))
                    forward()
                }
                item {
                    title(SettingItem.PlaySpeed.toTitle())
                    icon(R.drawable.ic_play_speed)
                    value(state.currentSpeed.toSpeedValue())
                    action(TaiwaAction.Anchor(speedAnchor))
                    forward()
                }
                item {
                    title("Опенинг и эндинг")
                    icon(R.drawable.ic_skip_forward)
                    action(TaiwaAction.Anchor(skipsAnchor))
                    forward()
                }
                switchItem {
                    title(SettingItem.InactiveTimer.toTitle())
                    subtitle("Отсчитывает 1 час")
                    icon(R.drawable.ic_timer_outline)
                    select(state.inactiveTimerEnabled)
                    onClick { onInactiveTimerSelected?.invoke(!state.inactiveTimerEnabled) }
                }
                switchItem {
                    title(SettingItem.Autoplay.toTitle())
                    icon(R.drawable.ic_play_circle_outline)
                    select(state.autoplayEnabled)
                    onClick { onAutoplaySelected?.invoke(!state.autoplayEnabled) }
                }
            }

            nestedContent(qualityAnchor) {
                header {
                    title(SettingItem.Quality.toTitle())
                    backAction(TaiwaAction.Root)
                    canClose()
                }
                items {
                    action(TaiwaAction.Root)
                    state.availableQualities.forEach { quality ->
                        radioItem(quality) {
                            title(quality.toValue())
                            icon(quality.toIcRes())
                            select(quality == state.currentQuality)
                            onClick { onQualitySelected?.invoke(quality) }
                        }
                    }
                }
            }

            nestedContent(speedAnchor) {
                header {
                    title(SettingItem.PlaySpeed.toTitle())
                    backAction(TaiwaAction.Root)
                    canClose()
                }
                items {
                    custom(SpeedSelector(state.currentSpeed))
                }
            }

            nestedContent(skipsAnchor) {
                header {
                    title("Опенинг и эндинг")
                    backAction(TaiwaAction.Root)
                    canClose()
                }
                items {
                    switchItem {
                        title(SettingItem.Skips.toTitle())
                        icon(R.drawable.ic_skip_forward)
                        select(state.skipsEnabled)
                        onClick { onSkipsSelected?.invoke(!state.skipsEnabled) }
                    }
                    switchItem {
                        title(SettingItem.SkipsTimer.toTitle())
                        icon(R.drawable.ic_av_timer)
                        select(state.skipsTimerEnabled)
                        onClick { onSkipsTimerSelected?.invoke(!state.skipsTimerEnabled) }
                    }
                }
            }
        }
    }

    private fun SettingItem.toTitle(): String = when (this) {
        SettingItem.Quality -> "Качество"
        SettingItem.PlaySpeed -> "Скорость"
        SettingItem.Skips -> "Кнопки пропуска"
        SettingItem.SkipsTimer -> "Автопропуск через 5 сек."
        SettingItem.InactiveTimer -> "Таймер на бездействие"
        SettingItem.Autoplay -> "Автовоспроизведение"
    }

    private fun PlayerQuality.toValue() = when (this) {
        PlayerQuality.SD -> "480p"
        PlayerQuality.HD -> "720p"
        PlayerQuality.FULLHD -> "1080p"
    }

    private fun PlayerQuality.toIcRes(): Int = when (this) {
        PlayerQuality.SD -> R.drawable.ic_quality_sd_base
        PlayerQuality.HD -> R.drawable.ic_quality_hd_base
        PlayerQuality.FULLHD -> R.drawable.ic_quality_full_hd_base
    }


    private enum class SettingItem {
        Quality,
        PlaySpeed,
        Skips,
        SkipsTimer,
        InactiveTimer,
        Autoplay
    }
}

private fun Float.toSpeedValue() = if (this == 1.0f) {
    "Нормальная"
} else {
    "${"${this}".trimEnd('0').trimEnd('.').trimEnd(',')}x"
}


private data class SpeedSelector(val speed: Float) : DiffItem("speed")

private fun speedSelectorEnvoy(
    speedListener: (Float) -> Unit
) = viewBindingEnvoy<SpeedSelector, ItemPlayerSpeedsBinding> {
    val speedFlow = MutableSharedFlow<Float>()

    fun Float.coerceSpeed() = coerceIn(0.25f, 2.0f)

    fun updateValue(block: (BigDecimal) -> BigDecimal) {
        val init = view.seekbar.value.toBigDecimal()
        val result = block.invoke(init).toFloat().coerceSpeed()
        view.seekbar.value = result
    }

    view.seekbar.apply {
        valueFrom = 0.25f
        valueTo = 2.0f
        stepSize = 0.05f
        addOnChangeListener { _, value, _ ->
            view.title.text = value.toSpeedValue()
            view.root.attachedCoroutineScope.launch {
                speedFlow.emit(value)
            }
        }
    }
    view.minusSpeed.setOnClickListener {
        updateValue { it - view.seekbar.stepSize.toBigDecimal() }
    }

    view.plusSpeed.setOnClickListener {
        updateValue { it + view.seekbar.stepSize.toBigDecimal() }
    }

    view.chip1.setOnClickListener {
        view.seekbar.value = 1f
    }

    view.chip2.setOnClickListener {
        view.seekbar.value = 1.25f
    }

    view.chip3.setOnClickListener {
        view.seekbar.value = 1.5f
    }

    view.chip4.setOnClickListener {
        view.seekbar.value = 1.75f
    }

    view.chip5.setOnClickListener {
        view.seekbar.value = 2.0f
    }

    attach {
        speedFlow
            .debounce(300)
            .onEach { speedListener.invoke(it) }
            .launchIn(view.root.attachedCoroutineScope)
    }

    bind {
        val coercedSpeed = it.speed.coerceSpeed()
        view.title.text = coercedSpeed.toSpeedValue()
        view.seekbar.value = coercedSpeed
    }
}

data class PlayerSettingsState(
    val currentSpeed: Float,
    val currentQuality: PlayerQuality,
    val availableQualities: Set<PlayerQuality>,
    val skipsEnabled: Boolean,
    val skipsTimerEnabled: Boolean,
    val inactiveTimerEnabled: Boolean,
    val autoplayEnabled: Boolean,
)