package ru.radiationx.anilibria.ui.fragments.auth.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentAuth2faCodeBinding
import ru.radiationx.anilibria.presentation.auth.Auth2FaCodeViewModel
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.shared.ktx.android.addTextChangeListener
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.di.injectDependencies
import ru.radiationx.shared_app.di.viewModel
import javax.inject.Inject

/**
 * Created by radiationx on 30.12.17.
 */
class Auth2FaCodeFragment :
    BaseFragment<FragmentAuth2faCodeBinding>(R.layout.fragment_auth_2fa_code) {

    companion object {
        private const val ARG_LOGIN = "arg_login"
        private const val ARG_PASSWORD = "arg_password"

        fun newInstance(login: String, password: String) = Auth2FaCodeFragment().putExtra {
            putString(ARG_LOGIN, login)
            putString(ARG_PASSWORD, password)
        }
    }

    private val viewModel by viewModel<Auth2FaCodeViewModel>()

    @Inject
    lateinit var apiConfig: ApiConfig

    @Inject
    lateinit var systemUtils: SystemUtils

    override fun onCreateBinding(view: View): FragmentAuth2faCodeBinding {
        return FragmentAuth2faCodeBinding.bind(view)
    }

    override val statusBarVisible: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.currentLogin = it.getString(ARG_LOGIN, viewModel.currentLogin)
            viewModel.currentPassword = it.getString(ARG_PASSWORD, viewModel.currentPassword)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseBinding.appbarLayout.gone()
        binding.btPasteClipboard.setOnClickListener {
            val value = systemUtils.readFromClipboard()
                .orEmpty()
                .replace(" ", "")
                .trim()
            if (value.isDigitsOnly() && value.length == 6) {
                binding.auth2facode.setText(value)
            } else {
                Toast.makeText(requireContext(), "Неправильный формат 2fa кода", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.authSubmit.setOnClickListener { viewModel.signIn() }
        binding.auth2facode.addTextChangeListener { viewModel.setCode2fa(it) }

        viewModel.state.onEach { state->
            binding.authSubmit.isEnabled = state.actionEnabled
            binding.authSwitcher.displayedChild = if (state.sending) 1 else 0
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}
