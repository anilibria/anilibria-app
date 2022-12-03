package ru.radiationx.anilibria.ui.fragments.configuring

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionSet
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentConfiguringBinding
import ru.radiationx.anilibria.presentation.configuring.ConfiguringViewModel
import ru.radiationx.anilibria.ui.fragments.BaseDimensionsFragment
import ru.radiationx.data.entity.common.ConfigScreenState
import ru.radiationx.quill.quillViewModel
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.visible

class ConfiguringFragment : BaseDimensionsFragment(R.layout.fragment_configuring) {

    private val binding by viewBinding<FragmentConfiguringBinding>()

    private val viewModel by quillViewModel<ConfiguringViewModel>()

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
        binding.configRefresh.visible(needRefresh)
        binding.configSkip.visible(needRefresh)
        binding.configNext.visible(needRefresh)
        binding.configProgress.gone(needRefresh)
    }
}