package ru.radiationx.anilibria.screen.player

import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import ru.radiationx.anilibria.databinding.ViewPlayerSkipsBinding
import ru.radiationx.data.entity.domain.release.PlayerSkips

class PlayerSkipsPart(
    private val parent: FrameLayout,
    private val onSeek: (Long) -> Unit,
    private val onSkipShow: () -> Unit,
    private val onSkipHide: () -> Unit,
) {

    private val binding = ViewPlayerSkipsBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        true
    )

    private var playerSkips: PlayerSkips? = null
    private val skippedList = mutableSetOf<PlayerSkips.Skip>()
    private var currentPosition = 0L
    private var currentSkipShow = false

    init {
        binding.btSkipsCancel.setOnClickListener {
            cancelSkip()
        }
        binding.btSkipsSkip.setOnClickListener {
            getCurrentSkip()?.also {
                onSeek(it.end)
            }
            cancelSkip()
        }
        binding.root.isVisible = false
    }

    fun setSkips(skips: PlayerSkips?) {
        playerSkips = skips
        skippedList.clear()
    }

    fun update(position: Long) {
        currentPosition = position
        autoCancel()
        val skip = getCurrentSkip()
        val hasSkip = skip != null
        binding.apply {
            if (hasSkip && (!btSkipsSkip.isFocused && !btSkipsCancel.isFocused)) {
                btSkipsSkip.requestFocus()
            }
        }
        if (hasSkip == currentSkipShow) {
            return
        }
        currentSkipShow = hasSkip
        if (hasSkip) {
            onSkipShow.invoke()
        } else {
            onSkipHide.invoke()
        }
        binding.root.isVisible = hasSkip
    }

    private fun getCurrentSkip(): PlayerSkips.Skip? {
        return playerSkips?.opening?.takeIf { checkSkip(it) }
            ?: playerSkips?.ending?.takeIf { checkSkip(it) }
    }

    private fun checkSkip(skip: PlayerSkips.Skip): Boolean {
        return !skippedList.contains(skip) && currentPosition >= skip.start && currentPosition <= skip.end
    }

    private fun autoCancel() {
        val opening = playerSkips?.opening
        val ending = playerSkips?.ending

        if (opening != null && opening !in skippedList && opening.end < currentPosition) {
            skippedList.add(opening)
        }

        if (ending != null && ending !in skippedList && ending.end < currentPosition) {
            skippedList.add(ending)
        }
    }

    private fun cancelSkip() {
        getCurrentSkip()?.also { skippedList.add(it) }
    }

}