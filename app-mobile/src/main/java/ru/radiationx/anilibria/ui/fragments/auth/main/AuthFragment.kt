package ru.radiationx.anilibria.ui.fragments.auth.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_auth.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.model.SocialAuthItemState
import ru.radiationx.anilibria.presentation.auth.AuthPresenter
import ru.radiationx.anilibria.presentation.auth.AuthView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.shared.ktx.android.addTextChangeListener
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.di.injectDependencies
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class AuthFragment : BaseFragment(), AuthView {

    private val socialAuthAdapter = SocialAuthAdapter {
        onSocialClick(it)
    }

    private val lifecycleTimeCounter by lazy {
        LifecycleTimeCounter(presenter::submitUseTime)
    }

    @Inject
    lateinit var apiConfig: ApiConfig

    @Inject
    lateinit var systemUtils: SystemUtils

    @InjectPresenter
    lateinit var presenter: AuthPresenter

    @ProvidePresenter
    fun provideAuthPresenter(): AuthPresenter =
        getDependency(AuthPresenter::class.java, screenScope)

    override fun getLayoutResource(): Int = R.layout.fragment_auth

    override val statusBarVisible: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(lifecycleTimeCounter)
        appbarLayout.gone()

        authSocialList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = socialAuthAdapter
            disableItemChangeAnimation()
        }

        authSubmit.setOnClickListener { presenter.signIn() }
        authSkip.setOnClickListener { presenter.skip() }
        authRegistration.setOnClickListener { presenter.registrationClick() }

        authLogin.addTextChangeListener { presenter.setLogin(it) }
        authPassword.addTextChangeListener { presenter.setPassword(it) }
    }

    override fun setSignButtonEnabled(isEnabled: Boolean) {
        authSubmit.isEnabled = isEnabled
    }

    override fun showRegistrationDialog() {
        context?.let {
            AlertDialog.Builder(it)
                .setMessage("Зарегистрировать аккаунт можно только на сайте.")
                .setPositiveButton("Регистрация") { _, _ ->
                    presenter.registrationToSiteClick()
                }
                .setNeutralButton("Отмена", null)
                .show()
        }
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return false
    }

    override fun setRefreshing(refreshing: Boolean) {
        authSwitcher.displayedChild = if (refreshing) 1 else 0
    }

    override fun showSocial(items: List<SocialAuthItemState>) {
        authSocialTop.visible(items.isNotEmpty())
        authSocialContent.visible(items.isNotEmpty())
        authSocialBottom.visible(items.isNotEmpty())
        socialAuthAdapter.bindItems(items)
    }

    private fun onSocialClick(item: SocialAuthItemState) {
        AlertDialog.Builder(requireContext())
            .setMessage("Обратите внимание, что в приложении возможна только авторизация, без регистрации аккаунта.\n\nЕсли ваши аккаунты не привязаны друг к другу, то зайдите в личный кабинет на сайте и привяжите их. ")
            .setPositiveButton("Продолжить") { _, _ ->
                presenter.onSocialClick(item)
            }
            .setNegativeButton("Личный кабинет") { _, _ ->
                systemUtils.externalLink("${apiConfig.siteUrl}/pages/cp.php")
            }
            .show()
    }

}
