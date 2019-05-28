package ru.radiationx.anilibria.ui.fragments.configuring

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_configuring.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.presentation.auth.AuthPresenter
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
    }

    override fun showStatus(status: String) {
        config_status.text = status
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return false
    }
}