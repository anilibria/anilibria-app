package ru.radiationx.anilibria.ui.fragments.configuring

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import androidx.transition.TransitionSet
import by.kirich1409.viewbindingdelegate.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentConfiguringBinding
import ru.radiationx.anilibria.presentation.configuring.ConfiguringPresenter
import ru.radiationx.anilibria.presentation.configuring.ConfiguringView
import ru.radiationx.anilibria.ui.fragments.ScopeFragment
import ru.radiationx.data.entity.common.ConfigScreenState
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.shared_app.di.injectDependencies

class ConfiguringFragment : ScopeFragment(R.layout.fragment_configuring), ConfiguringView {

    @InjectPresenter
    lateinit var presenter: ConfiguringPresenter

    @ProvidePresenter
    fun provideAuthPresenter(): ConfiguringPresenter =
        getDependency(ConfiguringPresenter::class.java)

    private val binding by viewBinding<FragmentConfiguringBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onViewCreated(view, savedInstanceState)
        binding.configRefresh.setOnClickListener { presenter.continueCheck() }
        binding.configSkip.setOnClickListener { presenter.skipCheck() }
        binding.configNext.setOnClickListener { presenter.nextCheck() }
    }

    override fun updateScreen(screenState: ConfigScreenState) {
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

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return false
    }
}