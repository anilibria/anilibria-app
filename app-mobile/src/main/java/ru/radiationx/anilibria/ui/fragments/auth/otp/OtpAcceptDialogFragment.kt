package ru.radiationx.anilibria.ui.fragments.auth.otp

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentOtpBinding
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.launchInResumed
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import taiwa.dialogs.CustomDialogFragment

class OtpAcceptDialogFragment : CustomDialogFragment(R.layout.fragment_otp) {

    private val useTimeCounter by lazy {
        LifecycleTimeCounter(viewModel::submitUseTime)
    }

    private val binding by viewBinding<FragmentOtpBinding>()

    private val viewModel by viewModel<OtpAcceptViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(useTimeCounter)
        binding.otpTitle.setOnClickListener {
            binding.otpHelp.isVisible = !binding.otpHelp.isVisible
        }
        binding.btAction.setOnClickListener {
            val code = binding.otpInputField.text?.toString().orEmpty()
            viewModel.onAcceptClick(code)
        }

        viewModel.state.onEach { state ->
            binding.otpInputLayout.also { TransitionManager.beginDelayedTransition(it) }

            binding.otpInputLayout.isInvisible = state.progress || state.success
            binding.otpProgress.isInvisible = !state.progress || state.success
            binding.otpSuccess.isInvisible = !state.success

            binding.otpInputLayout.isErrorEnabled = state.error != null
            binding.otpInputLayout.error = state.error

            if (binding.otpInputLayout.isVisible) {
                binding.otpInputLayout.requestFocus()
            }
            binding.btAction.isEnabled = !state.progress && !state.success
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.closeEvent.onEach {
            dismissAllowingStateLoss()
        }.launchInResumed(viewLifecycleOwner)
    }
}