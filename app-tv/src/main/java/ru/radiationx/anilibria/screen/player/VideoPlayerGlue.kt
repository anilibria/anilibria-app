/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("DEPRECATION")

package ru.radiationx.anilibria.screen.player

import android.content.Context
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.PlaybackControlsRow
import androidx.leanback.widget.PlaybackControlsRow.FastForwardAction
import androidx.leanback.widget.PlaybackControlsRow.MultiAction
import androidx.leanback.widget.PlaybackControlsRow.RewindAction
import androidx.leanback.widget.PlaybackControlsRow.SkipNextAction
import androidx.leanback.widget.PlaybackControlsRow.SkipPreviousAction
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import ru.radiationx.data.entity.common.PlayerQuality
import java.util.concurrent.TimeUnit


/**
 * Manages customizing the actions in the [PlaybackControlsRow].
 * Also adds custom actions for quality, speed, episodes, etc.
 */
@UnstableApi
class VideoPlayerGlue(
    context: Context,
    /**
     * Ссылка на наш [VideoSupportFragment], чтобы можно было управлять оверлеем.
     */
    private val fragment: VideoSupportFragment,
    playerAdapter: LeanbackPlayerAdapter,
) : PlaybackTransportControlGlue<LeanbackPlayerAdapter>(context, playerAdapter) {

    interface OnActionClickedListener {
        fun onPrevious()
        fun onNext()
        fun onQualityClick()
        fun onSpeedClick()
        fun onEpisodesClick()
    }

    interface PlaybackListener {
        fun onUpdateProgress()
    }

    var actionListener: OnActionClickedListener? = null
    var playbackListener: PlaybackListener? = null

    private val previousAction by lazy { SkipPreviousAction(context) }
    private val nextAction by lazy { SkipNextAction(context) }
    private val forwardAction by lazy { FastForwardAction(context) }
    private val rewindAction by lazy { RewindAction(context) }

    private val qualityAction by lazy { QualityAction(context) }
    private val speedAction by lazy { SpeedAction(context) }
    private val episodesAction by lazy { EpisodesAction(context) }

    init {
        isSeekEnabled = true
    }

    override fun onCreatePrimaryActions(adapter: ArrayObjectAdapter) {
        super.onCreatePrimaryActions(adapter)
        adapter.add(previousAction)
        adapter.add(nextAction)
    }

    override fun onCreateSecondaryActions(adapter: ArrayObjectAdapter) {
        super.onCreateSecondaryActions(adapter)
        // По умолчанию иконка качества у нас стоит на HD
        qualityAction.index = 1

        adapter.add(qualityAction)
        adapter.add(speedAction)
        adapter.add(episodesAction)
    }

    override fun onActionClicked(action: Action) {
        if (shouldDispatchAction(action)) {
            dispatchAction(action)
        } else {
            super.onActionClicked(action)
        }
    }

    override fun onUpdateProgress() {
        super.onUpdateProgress()
        playbackListener?.onUpdateProgress()
    }

    /**
     * Вызывается, когда переключаемся между Play/Pause.
     * Если вы нажимаете аппаратную кнопку Play/Pause и `BasePlayerFragment` «глотает»
     * это событие, Leanback может не запустить «автоскрытие» оверлея автоматически.
     *
     * Чтобы этого не случилось, мы явно перезапускаем показ + автоскрытие.
     */
    override fun onPlayStateChanged() {
        super.onPlayStateChanged()
        // Показываем оверлей
        fragment.showControlsOverlay(false)
        // Принудительно перезапускаем таймер автоскрытия
        fragment.isControlsOverlayAutoHideEnabled = false
        fragment.isControlsOverlayAutoHideEnabled = true
    }

    private fun shouldDispatchAction(action: Action): Boolean {
        return action === rewindAction ||
                action === forwardAction ||
                action === qualityAction ||
                action === speedAction ||
                action === episodesAction
    }

    private fun dispatchAction(action: Action) {
        when {
            action === rewindAction -> rewind()
            action === forwardAction -> fastForward()
            action === qualityAction -> actionListener?.onQualityClick()
            action === speedAction -> actionListener?.onSpeedClick()
            action === episodesAction -> actionListener?.onEpisodesClick()
            action is MultiAction -> {
                action.nextIndex()
                val row = controlsRow ?: return
                (row.secondaryActionsAdapter as? ArrayObjectAdapter)?.also {
                    notifyActionChanged(action, it)
                }
            }
        }
    }

    private fun notifyActionChanged(action: MultiAction, adapter: ArrayObjectAdapter) {
        val index = adapter.indexOf(action)
        if (index >= 0) {
            adapter.notifyArrayItemRangeChanged(index, 1)
        }
    }

    override fun next() {
        actionListener?.onNext()
    }

    override fun previous() {
        actionListener?.onPrevious()
    }

    /** Skips backwards 10 seconds.  */
    fun rewind() {
        var newPosition = currentPosition - TEN_SECONDS
        if (newPosition < 0) newPosition = 0
        playerAdapter?.seekTo(newPosition)
    }

    /** Skips forward 10 seconds.  */
    fun fastForward() {
        if (duration > -1) {
            var newPosition = currentPosition + TEN_SECONDS
            if (newPosition > duration) newPosition = duration
            playerAdapter?.seekTo(newPosition)
        }
    }

    /** Установить иконку качества (SD / HD / FULLHD) в панель управления. */
    fun setQuality(quality: PlayerQuality) {
        qualityAction.index = when (quality) {
            PlayerQuality.SD -> QualityAction.INDEX_SD
            PlayerQuality.HD -> QualityAction.INDEX_HD
            PlayerQuality.FULLHD -> QualityAction.INDEX_FHD
        }
        controlsRow?.let { row ->
            (row.secondaryActionsAdapter as? ArrayObjectAdapter)?.let { adapter ->
                notifyActionChanged(qualityAction, adapter)
            }
        }
    }

    companion object {
        private val TEN_SECONDS = TimeUnit.SECONDS.toMillis(10)
    }
}
