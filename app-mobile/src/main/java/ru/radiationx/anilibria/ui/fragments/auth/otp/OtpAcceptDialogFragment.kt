package ru.radiationx.anilibria.ui.fragments.auth.otp

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import by.kirich1409.viewbindingdelegate.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentOtpBinding
import ru.radiationx.anilibria.presentation.auth.otp.OtpAcceptPresenter
import ru.radiationx.anilibria.presentation.auth.otp.OtpAcceptView
import ru.radiationx.anilibria.ui.fragments.AlertDialogFragment
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import ru.radiationx.shared_app.di.getDependency

class OtpAcceptDialogFragment : AlertDialogFragment(R.layout.fragment_otp), OtpAcceptView {

    private val useTimeCounter by lazy {
        LifecycleTimeCounter(presenter::submitUseTime)
    }

    private val binding by viewBinding<FragmentOtpBinding>()

    @InjectPresenter
    lateinit var presenter: OtpAcceptPresenter

    @ProvidePresenter
    fun provideAuthPresenter(): OtpAcceptPresenter = getDependency(OtpAcceptPresenter::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(useTimeCounter)
        binding.otpTitle.setOnClickListener {
            binding.otpHelp.isVisible = !binding.otpHelp.isVisible
        }
        binding.btAction.setOnClickListener {
            val code = binding.otpInputField.text?.toString().orEmpty()
            presenter.onAcceptClick(code)
        }
    }

    override fun close() {
        dismiss()
    }

    override fun setState(success: Boolean, progress: Boolean, error: String?) {
        binding.otpInputLayout.also { TransitionManager.beginDelayedTransition(it) }

        binding.otpInputLayout.isInvisible = progress || success
        binding.otpProgress.isInvisible = !progress || success
        binding.otpSuccess.isInvisible = !success

        binding.otpInputLayout.isErrorEnabled = error != null
        binding.otpInputLayout.error = error

        if (binding.otpInputLayout.isVisible) {
            binding.otpInputLayout.requestFocus()
        }
        binding.btAction.isEnabled = !progress && !success
    }

    override fun setRefreshing(refreshing: Boolean) {
    }

}