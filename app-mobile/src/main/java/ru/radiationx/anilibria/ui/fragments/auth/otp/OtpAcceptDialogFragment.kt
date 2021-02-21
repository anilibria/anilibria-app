package ru.radiationx.anilibria.ui.fragments.auth.otp

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_otp.view.*
import moxy.MvpAppCompatDialogFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.auth.otp.OtpAcceptPresenter
import ru.radiationx.anilibria.presentation.auth.otp.OtpAcceptView
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import ru.radiationx.shared_app.di.getDependency

class OtpAcceptDialogFragment : MvpAppCompatDialogFragment(), OtpAcceptView {

    private var otpLayoutView: ConstraintLayout? = null
    private var otpInputLayout: TextInputLayout? = null
    private var otpInputField: TextInputEditText? = null
    private var otpProgressBar: ProgressBar? = null
    private var otpSuccess: AppCompatTextView? = null

    private val alertDialog: AlertDialog?
        get() = dialog as? AlertDialog?

    private val useTimeCounter by lazy {
        LifecycleTimeCounter(presenter::submitUseTime)
    }

    @InjectPresenter
    lateinit var presenter: OtpAcceptPresenter

    @ProvidePresenter
    fun provideAuthPresenter(): OtpAcceptPresenter = getDependency(OtpAcceptPresenter::class.java)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setPositiveButton("Привязать", null)
            .create()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val otpLayout =
            inflater.inflate(R.layout.fragment_otp, container, false) as ConstraintLayout
        otpLayoutView = otpLayout
        otpInputLayout = otpLayout.otpInputLayout
        otpInputField = otpLayout.otpInputField
        otpProgressBar = otpLayout.otpProgress
        otpSuccess = otpLayout.otpSuccess
        alertDialog?.setView(otpLayout)
        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(useTimeCounter)
    }

    override fun onStart() {
        super.onStart()
        alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            onPositiveClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        otpLayoutView = null
        otpInputLayout = null
        otpInputField = null
        otpProgressBar = null
        otpSuccess = null
    }

    override fun close() {
        dismiss()
    }

    override fun setState(success: Boolean, progress: Boolean, error: String?) {
        otpInputLayout?.also { TransitionManager.beginDelayedTransition(it) }

        otpInputLayout?.isInvisible = progress || success
        otpProgressBar?.isInvisible = !progress || success
        otpSuccess?.isInvisible = !success

        otpInputLayout?.isErrorEnabled = error != null
        otpInputLayout?.error = error

        if (otpInputLayout?.isVisible == true) {
            otpInputLayout?.requestFocus()
        }

        alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = !progress && !success
    }

    override fun setRefreshing(refreshing: Boolean) {
    }

    private fun onPositiveClick() {
        val code = otpInputField?.text?.toString() ?: return
        presenter.onAcceptClick(code)
    }
}