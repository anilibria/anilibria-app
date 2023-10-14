package ru.radiationx.anilibria.ui.fragments.auth.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentAuth2faCodeBinding
import ru.radiationx.anilibria.ui.fragments.BaseToolbarFragment
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.addTextChangeListener
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared_app.common.SystemUtils

/**
 * Created by radiationx on 30.12.17.
 */
class Auth2FaCodeFragment :
    BaseToolbarFragment<FragmentAuth2faCodeBinding>(R.layout.fragment_auth_2fa_code) {

    companion object {
        private const val ARG_LOGIN = "arg_login"
        private const val ARG_PASSWORD = "arg_password"

        fun newInstance(login: String, password: String) = Auth2FaCodeFragment().putExtra {
            putString(ARG_LOGIN, login)
            putString(ARG_PASSWORD, password)
        }
    }

    private val viewModel by viewModel<Auth2FaCodeViewModel> {
        Auth2FaCodeExtra(
            login = getExtraNotNull(ARG_LOGIN),
            password = getExtraNotNull(ARG_PASSWORD)
        )
    }

    private val systemUtils by inject<SystemUtils>()

    override fun onCreateBinding(view: View): FragmentAuth2faCodeBinding {
        return FragmentAuth2faCodeBinding.bind(view)
    }

    override val statusBarVisible: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseBinding.appbarLayout.isGone = true
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

        viewModel.state.onEach { state ->
            binding.authSubmit.isEnabled = state.actionEnabled
            binding.authSwitcher.displayedChild = if (state.sending) 1 else 0
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
