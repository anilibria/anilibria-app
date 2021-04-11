package ru.radiationx.anilibria.ui.fragments.donation.yoomoney

import android.graphics.Rect
import android.os.Bundle
import android.text.method.TransformationMethod
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.dialog_donation_yoomoney.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.donation.infra.DonationYooMoneyState
import ru.radiationx.anilibria.presentation.donation.yoomoney.DonationYooMoneyPresenter
import ru.radiationx.anilibria.presentation.donation.yoomoney.DonationYooMoneyView
import ru.radiationx.anilibria.ui.fragments.AlertDialogFragment
import ru.radiationx.data.entity.app.donation.donate.DonationYooMoneyInfo
import ru.radiationx.shared.ktx.android.addTextChangeListener
import ru.radiationx.shared.ktx.android.bindOptionalViews
import ru.radiationx.shared_app.di.getDependency

class DonationYooMoneyDialogFragment :
    AlertDialogFragment(R.layout.dialog_donation_yoomoney), DonationYooMoneyView {

    @InjectPresenter
    lateinit var presenter: DonationYooMoneyPresenter

    @ProvidePresenter
    fun providePresenter(): DonationYooMoneyPresenter =
        getDependency(DonationYooMoneyPresenter::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yooMoneyAmounts.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val intValue =
                    yooMoneyAmounts.findViewById<MaterialButton>(checkedId).getTextIntValue()
                presenter.setSelectedAmount(intValue)
            }
        }
        yooMoneyAmountInput.setOnFocusChangeListener { _, isFocused ->
            if (isFocused) {
                val intValue = yooMoneyAmountField.getTextIntValue()
                presenter.setCustomAmount(intValue)
            }
        }

        yooMoneyAmountField.addTextChangeListener {
            val intValue = yooMoneyAmountField.getTextIntValue()
            presenter.setCustomAmount(intValue)
        }

        yooMoneyTypes.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val paymentTypeId = when (checkedId) {
                    R.id.yooMoneyTypeAccount -> DonationYooMoneyInfo.TYPE_ID_ACCOUNT
                    R.id.yooMoneyTypeCard -> DonationYooMoneyInfo.TYPE_ID_CARD
                    R.id.yooMoneyTypeMobile -> DonationYooMoneyInfo.TYPE_ID_MOBILE
                    else -> null
                }
                if (paymentTypeId != null) {
                    presenter.setPaymentType(paymentTypeId)
                }
            }
        }

        yooMoneyAccept.setOnClickListener {
            presenter.onAcceptClick()
        }

        yooMoneyCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun closeView() {
        dismiss()
    }

    override fun showData(state: DonationYooMoneyState) {
        state.data?.also { bindData(it) }

        when (state.amountType) {
            DonationYooMoneyState.AmountType.PRESET -> {
                if (state.selectedAmount != null) {
                    yooMoneyAmounts.setSingleSelection(state.selectedAmount)
                } else {
                    yooMoneyAmounts.clearChecked()
                }
                yooMoneyAmountInput.clearFocus()
            }
            DonationYooMoneyState.AmountType.CUSTOM -> {
                yooMoneyAmounts.clearChecked()
                yooMoneyAmountInput.requestFocus()
            }
        }

        val selectedTypeViewId = when (state.selectedTypeId) {
            DonationYooMoneyInfo.TYPE_ID_ACCOUNT -> R.id.yooMoneyTypeAccount
            DonationYooMoneyInfo.TYPE_ID_CARD -> R.id.yooMoneyTypeCard
            DonationYooMoneyInfo.TYPE_ID_MOBILE -> R.id.yooMoneyTypeMobile
            else -> null
        }
        if (selectedTypeViewId != null) {
            yooMoneyTypes.setSingleSelection(selectedTypeViewId)
        } else {
            yooMoneyTypes.clearChecked()
        }

        yooMoneyAccept.isEnabled = state.acceptEnabled
    }

    private fun bindData(data: DonationYooMoneyInfo) {
        yooMoneyTitle.text = data.title

        val amountViews = listOf<View>(
            yooMoneyAmountTitle,
            yooMoneyAmounts,
            yooMoneyAmountInput
        )
        data.amounts.bindOptionalViews(amountViews) {
            yooMoneyAmountTitle.text = it.title
            yooMoneyAmountField.hint = it.hint
            updateAmountsViews(it.items)
        }

        data.paymentTypes.also { types ->
            yooMoneyTypeTitle.text = types.title

            val accountViews = listOf<View>(yooMoneyTypeAccount, yooMoneyTypeAccountName)
            val cardViews = listOf<View>(yooMoneyTypeCard, yooMoneyTypeCardName)
            val mobileViews = listOf<View>(yooMoneyTypeMobile, yooMoneyTypeMobileName)

            types.items
                .firstOrNull { it.id == DonationYooMoneyInfo.TYPE_ID_ACCOUNT }
                .bindOptionalViews(accountViews) {
                    yooMoneyTypeAccountName.text = it.title
                }
            types.items
                .firstOrNull { it.id == DonationYooMoneyInfo.TYPE_ID_CARD }
                .bindOptionalViews(cardViews) {
                    yooMoneyTypeAccountName.text = it.title
                }
            types.items
                .firstOrNull { it.id == DonationYooMoneyInfo.TYPE_ID_MOBILE }
                .bindOptionalViews(mobileViews) {
                    yooMoneyTypeAccountName.text = it.title
                }

            val hasSupportedTypes = types.items.any {
                it.id == DonationYooMoneyInfo.TYPE_ID_ACCOUNT ||
                        it.id == DonationYooMoneyInfo.TYPE_ID_CARD ||
                        it.id == DonationYooMoneyInfo.TYPE_ID_MOBILE
            }
            yooMoneyTypeTitle.isVisible = hasSupportedTypes
            yooMoneyTypes.isVisible = hasSupportedTypes
            yooMoneyTypesNames.isVisible = hasSupportedTypes
        }

        yooMoneyAccept.text = data.btDonateText
        yooMoneyCancel.text = data.btCancelText
    }

    private fun updateAmountsViews(amounts: List<Int>) {
        if (yooMoneyAmounts.childCount != amounts.size) {
            yooMoneyAmounts.clearChecked()
            yooMoneyAmounts.removeAllViews()

            amounts.forEach {
                val button = MaterialButton(
                    yooMoneyAmounts.context,
                    null,
                    R.attr.materialButtonOutlinedStyle
                )
                button.setPadding(0, 0, 0, 0)
                val layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                button.transformationMethod = AmountCurrencyTransformation()
                yooMoneyAmounts.addView(button, layoutParams)
            }

        }
        amounts.forEachIndexed { index, amount ->
            (yooMoneyAmounts.getChildAt(index) as MaterialButton?)?.also {
                it.id = amount
                it.text = amount.toString()
            }
        }
    }

    override fun setRefreshing(refreshing: Boolean) {
    }

    private fun TextView.getTextIntValue(): Int = text?.toString()?.toIntOrNull() ?: 0

    private class AmountCurrencyTransformation : TransformationMethod {

        override fun getTransformation(source: CharSequence?, view: View?): CharSequence {
            return source?.toString().orEmpty() + " ₽"
        }

        override fun onFocusChanged(p0: View?, p1: CharSequence?, p2: Boolean, p3: Int, p4: Rect?) {
        }
    }
}