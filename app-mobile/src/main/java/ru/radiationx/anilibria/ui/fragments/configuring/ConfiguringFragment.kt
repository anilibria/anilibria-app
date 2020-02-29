package ru.radiationx.anilibria.ui.fragments.configuring

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import androidx.transition.TransitionSet
import kotlinx.android.synthetic.main.fragment_configuring.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.injectDependencies
import ru.radiationx.anilibria.presentation.configuring.ConfiguringPresenter
import ru.radiationx.anilibria.presentation.configuring.ConfiguringView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.data.entity.common.ConfigScreenState
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.visible

class ConfiguringFragment : BaseFragment(), ConfiguringView {

    @InjectPresenter
    lateinit var presenter: ConfiguringPresenter

    @ProvidePresenter
    fun provideAuthPresenter(): ConfiguringPresenter = getDependency(ConfiguringPresenter::class.java, screenScope)

    override fun getBaseLayout(): Int = R.layout.fragment_configuring

    override val statusBarVisible: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onViewCreated(view, savedInstanceState)
        config_refresh.setOnClickListener { presenter.continueCheck() }
        config_skip.setOnClickListener { presenter.skipCheck() }
        config_next.setOnClickListener { presenter.nextCheck() }
    }

    override fun updateScreen(screenState: ConfigScreenState) {
        config_status.text = screenState.status
        config_next.text = if (screenState.hasNext) {
            "Следующий шаг"
        } else {
            null
        }

        TransitionManager.beginDelayedTransition(constraint, AutoTransition().apply {
            duration = 225
            ordering = TransitionSet.ORDERING_TOGETHER
        })
        val needRefresh = screenState.needRefresh
        config_refresh.visible(needRefresh)
        config_skip.visible(needRefresh)
        config_next.visible(needRefresh && screenState.hasNext)
        config_progress.gone(needRefresh)
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return false
    }
}