package ru.radiationx.anilibria.ui.fragments.auth.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import kotlinx.android.synthetic.main.fragment_auth_2fa_code.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.auth.Auth2FaCodePresenter
import ru.radiationx.anilibria.presentation.auth.Auth2FaCodeView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.shared.ktx.android.addTextChangeListener
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared_app.di.injectDependencies
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class Auth2FaCodeFragment : BaseFragment(), Auth2FaCodeView {

    companion object {
        private const val ARG_LOGIN = "arg_login"
        private const val ARG_PASSWORD = "arg_password"

        fun newInstance(login: String, password: String) = Auth2FaCodeFragment().putExtra {
            putString(ARG_LOGIN, login)
            putString(ARG_PASSWORD, password)
        }
    }

    @Inject
    lateinit var apiConfig: ApiConfig

    @InjectPresenter
    lateinit var presenter: Auth2FaCodePresenter

    @ProvidePresenter
    fun provideAuthPresenter(): Auth2FaCodePresenter =
        getDependency(Auth2FaCodePresenter::class.java, screenScope)

    override fun getLayoutResource(): Int = R.layout.fragment_auth_2fa_code

    override val statusBarVisible: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.let {
            presenter.currentLogin = it.getString(ARG_LOGIN, presenter.currentLogin)
            presenter.currentPassword = it.getString(ARG_PASSWORD, presenter.currentPassword)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appbarLayout.gone()
        btPasteClipboard.setOnClickListener {
            val value = Utils.readFromClipboard()
                .orEmpty()
                .replace(" ", "")
                .trim()
            if (value.isDigitsOnly() && value.length == 6) {
                auth2facode.setText(value)
            } else {
                Toast.makeText(requireContext(), "Неправильный формат 2fa кода", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        authSubmit.setOnClickListener { presenter.signIn() }
        auth2facode.addTextChangeListener { presenter.setCode2fa(it) }
    }

    override fun setSignButtonEnabled(isEnabled: Boolean) {
        authSubmit.isEnabled = isEnabled
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun setRefreshing(refreshing: Boolean) {
        authSwitcher.displayedChild = if (refreshing) 1 else 0
    }
}
