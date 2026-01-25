package ru.radiationx.anilibria.ui.fragments.auth.main

import android.os.Bundle
import android.view.View
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
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.fragments.BaseToolbarFragment
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.addTextChangeListener
import ru.radiationx.shared.ktx.android.launchInResumed
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import ru.radiationx.shared_app.common.SystemUtils
import taiwa.TaiwaAction
import taiwa.bottomsheet.bottomSheetTaiwa

/**
 * Created by radiationx on 30.12.17.
 */
class AuthFragment : BaseToolbarFragment<FragmentAuthBinding>(R.layout.fragment_auth),
    BackButtonListener {

    private val socialAuthAdapter = SocialAuthAdapter {
        onSocialClick(it)
    }

    private val lifecycleTimeCounter by lazy {
        LifecycleTimeCounter(viewModel::submitUseTime)
    }

    private val viewModel by viewModel<AuthViewModel>()

    private val apiConfig by inject<ApiConfig>()

    private val systemUtils by inject<SystemUtils>()

    private val socialTaiwa by bottomSheetTaiwa()

    private val registrationTaiwa by bottomSheetTaiwa {
        body {
            message {
                text("Зарегистрировать аккаунт можно только на сайте.")
            }
            buttons {
                action(TaiwaAction.Close)
                button {
                    text("Регистрация")
                    onClick { viewModel.registrationToSiteClick() }
                }
                button {
                    text("Отмена")
                }
            }
        }
    }

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
        }.launchInResumed(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.authSocialList.adapter = null
    }

    override fun onBackPressed(): Boolean {
        viewModel.onBackPressed()
        return true
    }

    private fun onSocialClick(item: SocialAuthItemState) {
        socialTaiwa.setContent {
            body {
                message {
                    text("Обратите внимание, что в приложении возможна только авторизация, без регистрации аккаунта.\n\nЕсли ваши аккаунты не привязаны друг к другу, то зайдите в личный кабинет на сайте и привяжите их. ")
                }
                buttons {
                    action(TaiwaAction.Close)
                    button {
                        text("Продолжить")
                        onClick { viewModel.onSocialClick(item) }
                    }
                    button {
                        text("Личный кабинет")
                        onClick { systemUtils.externalLink("${apiConfig.siteUrl}/pages/cp.php") }
                    }
                }
            }
        }
        socialTaiwa.show()
    }

    private fun showRegistrationDialog() {
        registrationTaiwa.show()
    }
}
