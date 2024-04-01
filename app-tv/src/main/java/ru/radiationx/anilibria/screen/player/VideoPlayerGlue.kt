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
 * Manages customizing the actions in the [PlaybackControlsRow]. Adds and manages the
 * following actions to the primary and secondary controls:
 *
 *
 *  * [androidx.leanback.widget.PlaybackControlsRow.RepeatAction]
 *  * [androidx.leanback.widget.PlaybackControlsRow.ThumbsDownAction]
 *  * [androidx.leanback.widget.PlaybackControlsRow.ThumbsUpAction]
 *  * [androidx.leanback.widget.PlaybackControlsRow.SkipPreviousAction]
 *  * [androidx.leanback.widget.PlaybackControlsRow.SkipNextAction]
 *  * [androidx.leanback.widget.PlaybackControlsRow.FastForwardAction]
 *  * [androidx.leanback.widget.PlaybackControlsRow.RewindAction]
 *
 *
 * Note that the superclass, [PlaybackTransportControlGlue], manages the playback controls
 * row.
 */
@UnstableApi
class VideoPlayerGlue(
    context: Context,
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

    override fun onUpdateProgress() {
        super.onUpdateProgress()
        playbackListener?.onUpdateProgress()
    }

    init {
        isSeekEnabled = true
    }

    override fun onCreatePrimaryActions(adapter: ArrayObjectAdapter) {
        super.onCreatePrimaryActions(adapter)
        adapter.add(previousAction)
        //adapter.add(mRewindAction);
        //adapter.add(mFastForwardAction);
        adapter.add(nextAction)
    }

    override fun onCreateSecondaryActions(adapter: ArrayObjectAdapter) {
        super.onCreateSecondaryActions(adapter)
        qualityAction.index = 1
        adapter.add(qualityAction)
        adapter.add(speedAction)
        adapter.add(episodesAction)
    }

    override fun onActionClicked(action: Action) {
        if (shouldDispatchAction(action)) {
            dispatchAction(action)
            return
        }
        super.onActionClicked(action)
    }

    private fun shouldDispatchAction(action: Action): Boolean {
        return action === rewindAction || action === forwardAction || action === qualityAction || action === speedAction || action === episodesAction
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
                // Notify adapter of action changes to handle secondary actions, such as, thumbs up/down
                // and repeat.
                controlsRow?.also {
                    notifyActionChanged(
                        action,
                        it.secondaryActionsAdapter as ArrayObjectAdapter
                    )
                }
            }
        }
    }

    private fun notifyActionChanged(
        action: MultiAction, adapter: ArrayObjectAdapter,
    ) {
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
        newPosition = if (newPosition < 0) 0 else newPosition
        playerAdapter!!.seekTo(newPosition)
    }

    /** Skips forward 10 seconds.  */
    fun fastForward() {
        if (duration > -1) {
            var newPosition = currentPosition + TEN_SECONDS
            newPosition = if (newPosition > duration) duration else newPosition
            playerAdapter!!.seekTo(newPosition)
        }
    }

    fun setQuality(quality: PlayerQuality) {
        qualityAction.index = when (quality) {
            PlayerQuality.SD -> QualityAction.INDEX_SD
            PlayerQuality.HD -> QualityAction.INDEX_HD
            PlayerQuality.FULLHD -> QualityAction.INDEX_FHD
        }
        controlsRow?.also {
            notifyActionChanged(
                qualityAction,
                it.secondaryActionsAdapter as ArrayObjectAdapter
            )
        }
    }

    companion object {
        private val TEN_SECONDS = TimeUnit.SECONDS.toMillis(10)
    }

}