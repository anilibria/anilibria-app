package ru.radiationx.anilibria.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.databinding.MergeProgressButtonBinding
import ru.radiationx.shared.ktx.android.setCompatDrawable

class ProgressButton @JvmOverloads constructor(
    context: Context,
    attrs:
    AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding by viewBinding<MergeProgressButtonBinding>(attachToRoot = true)
    private var progressJob: Job? = null

    var actionClickListener: (() -> Unit)? = null
    var cancelClickListener: (() -> Unit)? = null

    init {
        binding.viewProgressAction.setOnClickListener {
            actionClickListener?.invoke()
        }
        binding.viewProgressCancel.setOnClickListener {
            cancelClickListener?.invoke()
        }
    }

    fun setActionIconRes(@DrawableRes iconRes: Int) {
        binding.viewProgressAction.setCompatDrawable(iconRes)
    }

    fun bindProgress(progress: StateFlow<Int>?) {
        binding.viewProgressCancel.isVisible = progress != null
        binding.viewProgressAction.isVisible = progress == null
        binding.viewProgressIndicator.isVisible = progress != null
        bindProgressFlow(progress)
    }

    private fun bindProgressFlow(progressFlow: StateFlow<Int>?) {
        progressJob?.cancel()
        progressJob = null
        if (progressFlow == null) {
            return
        }
        val lifecycleOwner = binding.root.findViewTreeLifecycleOwner() ?: return
        progressJob = progressFlow.onEach {
            binding.viewProgressIndicator.progress = it
            binding.viewProgressIndicator.isIndeterminate = it <= 0
        }.launchIn(lifecycleOwner.lifecycleScope)
    }

}
