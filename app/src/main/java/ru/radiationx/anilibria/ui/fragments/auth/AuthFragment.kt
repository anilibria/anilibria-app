package ru.radiationx.anilibria.ui.fragments.auth

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_auth.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.entity.app.auth.SocialAuth
import ru.radiationx.anilibria.extension.addTextChangeListener
import ru.radiationx.anilibria.extension.gone
import ru.radiationx.anilibria.extension.visible
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.presentation.auth.AuthPresenter
import ru.radiationx.anilibria.presentation.auth.AuthView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.utils.Utils
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class AuthFragment : BaseFragment(), AuthView {

    private val socialAuthAdapter = SocialAuthAdapter {
        onSocialClick(it)
    }

    @Inject
    lateinit var apiConfig: ApiConfig

    @InjectPresenter
    lateinit var presenter: AuthPresenter

    @ProvidePresenter
    fun provideAuthPresenter(): AuthPresenter = getDependency(screenScope, AuthPresenter::class.java)

    override fun getLayoutResource(): Int = R.layout.fragment_auth

    override val statusBarVisible: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onViewCreated(view, savedInstanceState)

        appbarLayout.gone()

        authSocialList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = socialAuthAdapter
        }

        authSubmit.setOnClickListener { presenter.signIn() }
        authSkip.setOnClickListener { presenter.skip() }
        authRegistration.setOnClickListener { presenter.registrationClick() }

        authLogin.addTextChangeListener { presenter.setLogin(it) }
        authPassword.addTextChangeListener { presenter.setPassword(it) }
        auth2facode.addTextChangeListener { presenter.setCode2fa(it) }
    }

    override fun setSignButtonEnabled(isEnabled: Boolean) {
        authSubmit.isEnabled = isEnabled
    }

    override fun showRegistrationDialog() {
        context?.let {
            AlertDialog.Builder(it)
                    .setMessage("Зарегистрировать аккаунт можно только на сайте.")
                    .setPositiveButton("Регистрация") { _, _ ->
                        Utils.externalLink("${apiConfig.siteUrl}/pages/login.php")
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

    override fun showSocial(items: List<SocialAuth>) {
        authSocialTop.visible(items.isNotEmpty())
        authSocialContent.visible(items.isNotEmpty())
        authSocialBottom.visible(items.isNotEmpty())
        socialAuthAdapter.bindItems(items)
    }

    private fun onSocialClick(item: SocialAuth) {
        AlertDialog.Builder(context!!)
                .setMessage("Обратите внимание, что в приложении возможна только авторизация, без регистрации аккаунта.\n\nЕсли ваши аккаунты не привязаны друг к другу, то зайдите в личный кабинет на сайте и привяжите их. ")
                .setPositiveButton("Продолжить") { _, _ ->
                    presenter.onSocialClick(item)
                }
                .setNegativeButton("Личный кабинет") { _, _ ->
                    Utils.externalLink("${apiConfig.siteUrl}/pages/cp.php")
                }
                .show()
    }

}
