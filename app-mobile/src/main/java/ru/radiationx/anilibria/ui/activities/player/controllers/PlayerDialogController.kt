package ru.radiationx.anilibria.ui.activities.player.controllers

import android.content.Context
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import androidx.core.text.parseAsHtml
import androidx.lifecycle.LifecycleOwner
import org.michaelbel.bottomsheet.BottomSheet
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.apptheme.AppThemeController
import ru.radiationx.anilibria.extension.isDark
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.shared.ktx.android.getColorFromAttr
import ru.radiationx.shared.ktx.android.getCompatDrawable
import ru.radiationx.shared.ktx.android.showWithLifecycle

class PlayerDialogController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val appThemeController: AppThemeController,
) {

    var onQualitySelected: ((PlayerQuality) -> Unit)? = null
    var onSpeedSelected: ((Float) -> Unit)? = null
    var onEpisodeSelected: ((EpisodeId) -> Unit)? = null
    var onSkipsSelected: ((Boolean) -> Unit)? = null
    var onSkipsTimerSelected: ((Boolean) -> Unit)? = null
    var onInactiveTimerSelected: ((Boolean) -> Unit)? = null

    fun showPlaylist(episodes: List<Episode>, episodeId: EpisodeId) {
        val titles = episodes
            .map {
                if (it.id == episodeId) {
                    "<b>• ${it.title.orEmpty()}</b>"
                } else {
                    it.title.orEmpty()
                }
            }
            .map { it.parseAsHtml() }
            .toTypedArray()
        BottomSheet.Builder(context)
            .setItems(titles) { _, which ->
                onEpisodeSelected?.invoke(episodes[which].id)
            }
            .setTargetItemIndex(episodes.indexOfFirst { it.id == episodeId })
            .applyStyle()
            .showAndRegister()
    }

    fun showSettingsDialog(state: PlayerSettingsState) {
        val valuesList = SettingItem.entries

        val titles = valuesList
            .map {
                when (it) {
                    SettingItem.Quality -> "Качество (<b>${state.currentQuality.toTitle()}</b>)"
                    SettingItem.PlaySpeed -> "Скорость (<b>${state.currentSpeed.toSpeedTitle()}</b>)"
                    SettingItem.Skips -> "Кнопки пропуска опенинга (<b>${state.skipsEnabled.toTitle()}</b>)"
                    SettingItem.SkipsTimer -> "Автоматически пропускать опенинг (<b>${state.skipsTimerEnabled.toTitle()}</b>)"
                    SettingItem.InactiveTimer -> "Таймер на бездействие (<b>${state.inactiveTimerEnabled.toTitle()}</b>)"
                }
            }
            .map { it.parseAsHtml() }
            .toTypedArray()

        val icons = valuesList
            .map { value ->
                when (value) {
                    SettingItem.Quality -> state.currentQuality.toIcRes()
                    SettingItem.PlaySpeed -> R.drawable.ic_play_speed
                    SettingItem.Skips -> R.drawable.ic_skip_forward
                    SettingItem.SkipsTimer -> R.drawable.ic_av_timer
                    SettingItem.InactiveTimer -> R.drawable.ic_timer_outline
                }.let {
                    ContextCompat.getDrawable(context, it)
                }
            }
            .toTypedArray()

        BottomSheet.Builder(context)
            .setItems(titles, icons) { _, which ->
                when (valuesList[which]) {
                    SettingItem.Quality -> {
                        showQualityDialog(
                            state.currentQuality,
                            state.availableQualities.toList()
                        )
                    }

                    SettingItem.PlaySpeed -> {
                        showPlaySpeedDialog(state.currentSpeed)
                    }

                    SettingItem.Skips -> {
                        onSkipsSelected?.invoke(!state.skipsEnabled)
                    }

                    SettingItem.SkipsTimer -> {
                        onSkipsTimerSelected?.invoke(!state.skipsTimerEnabled)
                    }

                    SettingItem.InactiveTimer -> {
                        onInactiveTimerSelected?.invoke(!state.inactiveTimerEnabled)
                    }
                }
            }
            .applyStyle()
            .showAndRegister()
    }

    private fun showPlaySpeedDialog(currentPlaySpeed: Float) {
        val values = arrayOf(
            0.25f,
            0.5f,
            0.75f,
            1.0f,
            1.25f,
            1.5f,
            1.75f,
            2.0f
        )
        val activeIndex = values.indexOf(currentPlaySpeed)
        val titles = values
            .mapIndexed { index, s ->
                val stringValue = s.toSpeedTitle()
                when (index) {
                    activeIndex -> "<b>$stringValue</b>"
                    else -> stringValue
                }
            }
            .map { it.parseAsHtml() }
            .toTypedArray()

        BottomSheet.Builder(context)
            .setTitle("Скорость воспроизведения")
            .setItems(titles) { _, which ->
                onSpeedSelected?.invoke(values[which])
            }
            .applyStyle()
            .showAndRegister()
    }

    private fun showQualityDialog(current: PlayerQuality, available: List<PlayerQuality>) {
        val activeIndex = available.indexOf(current)
        val titles = available
            .mapIndexed { index, s ->
                val stringValue = s.toTitle()
                if (index == activeIndex) "<b>$stringValue</b>" else stringValue
            }
            .map { it.parseAsHtml() }
            .toTypedArray()

        val icons = available
            .map { context.getCompatDrawable(it.toIcRes()) }
            .toTypedArray()

        BottomSheet.Builder(context)
            .setTitle("Качество")
            .setItems(titles, icons) { _, which ->
                onQualitySelected?.invoke(available[which])
            }
            .applyStyle()
            .showAndRegister()
    }


    private fun BottomSheet.Builder.showAndRegister(): BottomSheet {
        val dialog = create()
        dialog.showWithLifecycle(lifecycleOwner)
        return dialog
    }

    private fun BottomSheet.Builder.applyStyle(): BottomSheet.Builder {
        setDarkTheme(appThemeController.getTheme().isDark())
        setIconTintMode(PorterDuff.Mode.SRC_ATOP)
        setIconColor(context.getColorFromAttr(R.attr.colorOnSurface))
        setItemTextColor(context.getColorFromAttr(R.attr.colorOnSurface))
        setTitleTextColor(context.getColorFromAttr(R.attr.colorOnSurface))
        setBackgroundColor(context.getColorFromAttr(R.attr.colorSurface))
        return this
    }

    private fun PlayerQuality.toTitle() = when (this) {
        PlayerQuality.SD -> "480p"
        PlayerQuality.HD -> "720p"
        PlayerQuality.FULLHD -> "1080p"
    }

    private fun PlayerQuality.toIcRes(): Int = when (this) {
        PlayerQuality.SD -> R.drawable.ic_quality_sd_base
        PlayerQuality.HD -> R.drawable.ic_quality_hd_base
        PlayerQuality.FULLHD -> R.drawable.ic_quality_full_hd_base
    }

    private fun Float.toSpeedTitle() = if (this == 1.0f) {
        "Обычная"
    } else {
        "${"${this}".trimEnd('0').trimEnd('.').trimEnd(',')}x"
    }

    private fun Boolean.toTitle() = if (this) "Да" else "Нет"

    private enum class SettingItem {
        Quality,
        PlaySpeed,
        Skips,
        SkipsTimer,
        InactiveTimer
    }
}

data class PlayerSettingsState(
    val currentSpeed: Float,
    val currentQuality: PlayerQuality,
    val availableQualities: Set<PlayerQuality>,
    val skipsEnabled: Boolean,
    val skipsTimerEnabled: Boolean,
    val inactiveTimerEnabled: Boolean,
)