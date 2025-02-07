package ru.radiationx.anilibria.ui.fragments.configuring

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionSet
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentConfiguringBinding
import ru.radiationx.anilibria.ui.fragments.BaseDimensionsFragment
import ru.radiationx.data.entity.common.ConfigScreenState
import ru.radiationx.quill.viewModel

class ConfiguringFragment : BaseDimensionsFragment(R.layout.fragment_configuring) {

    private val binding by viewBinding<FragmentConfiguringBinding>()

    private val viewModel by viewModel<ConfiguringViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.configRefresh.setOnClickListener { viewModel.continueCheck() }
        binding.configSkip.setOnClickListener { viewModel.skipCheck() }
        binding.configNext.setOnClickListener { viewModel.nextCheck() }

        viewModel.state.onEach { state ->
            updateScreen(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun updateScreen(screenState: ConfigScreenState) {
        binding.configStatus.text = screenState.status
        binding.configNext.text = if (screenState.hasNext) {
            "Следующий шаг"
        } else {
            "Начать проверку заново"
        }

        TransitionManager.beginDelayedTransition(binding.constraint, AutoTransition().apply {
            duration = 225
            ordering = TransitionSet.ORDERING_TOGETHER
        })
        val needRefresh = screenState.needRefresh
        binding.configRefresh.isVisible = needRefresh
        binding.configSkip.isVisible = needRefresh
        binding.configNext.isVisible = needRefresh
        binding.configProgress.isGone = needRefresh
    }
}