package ru.radiationx.anilibria.ui.fragments.auth.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentAuthBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.model.SocialAuthItemState
import ru.radiationx.anilibria.ui.fragments.BaseToolbarFragment
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.addTextChangeListener
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import ru.radiationx.shared_app.common.SystemUtils

/**
 * Created by radiationx on 30.12.17.
 */
class AuthFragment : BaseToolbarFragment<FragmentAuthBinding>(R.layout.fragment_auth) {

    private val socialAuthAdapter = SocialAuthAdapter {
        onSocialClick(it)
    }

    private val lifecycleTimeCounter by lazy {
        LifecycleTimeCounter(viewModel::submitUseTime)
    }

    private val viewModel by viewModel<AuthViewModel>()

    private val apiConfig by inject<ApiConfig>()

    private val systemUtils by inject<SystemUtils>()

    override val statusBarVisible: Boolean = true

    override fun onCreateBinding(view: View): FragmentAuthBinding {
        return FragmentAuthBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(lifecycleTimeCounter)
        baseBinding.appbarLayout.isGone = true

        binding.authSocialList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = socialAuthAdapter
            disableItemChangeAnimation()
        }

        binding.authSubmit.setOnClickListener { viewModel.signIn() }
        binding.authSkip.setOnClickListener { viewModel.skip() }
        binding.authRegistration.setOnClickListener { viewModel.registrationClick() }

        binding.authLogin.addTextChangeListener { viewModel.setLogin(it) }
        binding.authPassword.addTextChangeListener { viewModel.setPassword(it) }

        viewModel.state.onEach { state ->
            val socialItems = state.socialItems

            binding.authSubmit.isEnabled = state.actionEnabled
            binding.authSwitcher.displayedChild = if (state.sending) 1 else 0

            binding.authSocialTop.isVisible = socialItems.isNotEmpty()
            binding.authSocialContent.isVisible = socialItems.isNotEmpty()
            binding.authSocialBottom.isVisible = socialItems.isNotEmpty()
            socialAuthAdapter.bindItems(socialItems)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.registrationEvent.onEach {
            showRegistrationDialog()
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onSocialClick(item: SocialAuthItemState) {
        AlertDialog.Builder(requireContext())
            .setMessage("Обратите внимание, что в приложении возможна только авторизация, без регистрации аккаунта.\n\nЕсли ваши аккаунты не привязаны друг к другу, то зайдите в личный кабинет на сайте и привяжите их. ")
            .setPositiveButton("Продолжить") { _, _ ->
                viewModel.onSocialClick(item)
            }
            .setNegativeButton("Личный кабинет") { _, _ ->
                systemUtils.externalLink("${apiConfig.siteUrl}/pages/cp.php")
            }
            .show()
    }

    private fun showRegistrationDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Зарегистрировать аккаунт можно только на сайте.")
            .setPositiveButton("Регистрация") { _, _ ->
                viewModel.registrationToSiteClick()
            }
            .setNeutralButton("Отмена", null)
            .show()
    }
}
