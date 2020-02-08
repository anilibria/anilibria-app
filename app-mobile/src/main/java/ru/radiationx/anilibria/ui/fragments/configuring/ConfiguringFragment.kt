package ru.radiationx.anilibria.ui.fragments.configuring

import android.os.Bundle
import android.support.transition.TransitionSet
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_configuring.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.anilibria.presentation.configuring.ConfiguringPresenter
import ru.radiationx.anilibria.presentation.configuring.ConfiguringView
import ru.radiationx.anilibria.ui.fragments.BaseFragment

class ConfiguringFragment : BaseFragment(), ConfiguringView {

    @InjectPresenter
    lateinit var presenter: ConfiguringPresenter

    @ProvidePresenter
    fun provideAuthPresenter(): ConfiguringPresenter = getDependency(screenScope, ConfiguringPresenter::class.java)

    override fun getBaseLayout(): Int = R.layout.fragment_configuring

    override val statusBarVisible: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onViewCreated(view, savedInstanceState)
        config_refresh.setOnClickListener { presenter.continueCheck() }
        config_skip.setOnClickListener { presenter.skipCheck() }
        config_next.setOnClickListener { presenter.nextCheck() }
    }

    override fun updateScreen(screenState: ConfiguringPresenter.ScreenState) {
        config_status.text = screenState.status
        config_next.text = screenState.nextButton

        TransitionManager.beginDelayedTransition(constraint, AutoTransition().apply {
            duration = 225
            ordering = TransitionSet.ORDERING_TOGETHER
        })
        val isVisible = screenState.refresh
        config_refresh.visible(isVisible)
        config_skip.visible(isVisible)
        config_next.visible(isVisible && screenState.nextButton != null)
        config_progress.gone(isVisible)
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return false
    }
}