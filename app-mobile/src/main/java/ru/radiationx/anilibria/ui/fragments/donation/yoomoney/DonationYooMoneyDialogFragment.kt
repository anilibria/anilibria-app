package ru.radiationx.anilibria.ui.fragments.donation.yoomoney

import android.graphics.Rect
import android.os.Bundle
import android.text.method.TransformationMethod
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.DialogDonationYoomoneyBinding
import ru.radiationx.anilibria.ui.fragments.AlertDialogFragment
import ru.radiationx.data.entity.domain.donation.yoomoney.YooMoneyDialog
import ru.radiationx.quill.quillViewModel
import ru.radiationx.shared.ktx.android.addTextChangeListener
import ru.radiationx.shared.ktx.android.bindOptionalViews

class DonationYooMoneyDialogFragment : AlertDialogFragment(R.layout.dialog_donation_yoomoney) {

    private val binding by viewBinding<DialogDonationYoomoneyBinding>()

    private val viewModel by quillViewModel<DonationYooMoneyViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.yooMoneyAmounts.addCheckedListener { checkedId ->
            val intValue = checkedId
                ?.let { binding.yooMoneyAmounts.findViewById<MaterialButton>(it) }
                ?.getTextIntValue()
            viewModel.setSelectedAmount(intValue)
        }
        binding.yooMoneyAmountField.setOnFocusChangeListener { _, isFocused ->
            if (isFocused) {
                val intValue = binding.yooMoneyAmountField.getTextIntValue()
                viewModel.setCustomAmount(intValue)
            }
        }

        binding.yooMoneyAmountField.addTextChangeListener {
            val intValue = binding.yooMoneyAmountField.getTextIntValue()
            viewModel.setCustomAmount(intValue)
        }

        binding.yooMoneyTypes.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && checkedId != View.NO_ID) {
                val paymentTypeId = when (checkedId) {
                    R.id.yooMoneyTypeAccount -> YooMoneyDialog.TYPE_ID_ACCOUNT
                    R.id.yooMoneyTypeCard -> YooMoneyDialog.TYPE_ID_CARD
                    R.id.yooMoneyTypeMobile -> YooMoneyDialog.TYPE_ID_MOBILE
                    else -> null
                }
                if (paymentTypeId != null) {
                    viewModel.setPaymentType(paymentTypeId)
                }
            }
        }

        binding.yooMoneyAccept.setOnClickListener {
            viewModel.onAcceptClick()
        }

        binding.yooMoneyCancel.setOnClickListener {
            dismiss()
        }

        viewModel.state.onEach { state ->
            state.data?.also { bindData(it) }
            bindState(state)

        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.closeEvent.onEach {
            dismiss()
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun bindState(state: DonationYooMoneyState) {
        binding.yooMoneyProgress.isVisible = state.sending
        binding.yooMoneyAccept.isVisible = !state.sending
        when (state.amountType) {
            DonationYooMoneyState.AmountType.PRESET -> {
                if (state.selectedAmount != null) {
                    binding.yooMoneyAmounts.check(state.selectedAmount)
                } else {
                    binding.yooMoneyAmounts.clearChecked()
                }
                binding.yooMoneyAmountField.clearFocus()
            }
            DonationYooMoneyState.AmountType.CUSTOM -> {
                binding.yooMoneyAmounts.clearChecked()
                binding.yooMoneyAmountField.requestFocus()
            }
        }

        val selectedTypeViewId = when (state.selectedPaymentTypeId) {
            YooMoneyDialog.TYPE_ID_ACCOUNT -> R.id.yooMoneyTypeAccount
            YooMoneyDialog.TYPE_ID_CARD -> R.id.yooMoneyTypeCard
            YooMoneyDialog.TYPE_ID_MOBILE -> R.id.yooMoneyTypeMobile
            else -> null
        }
        if (selectedTypeViewId != null) {
            binding.yooMoneyTypes.check(selectedTypeViewId)
        } else {
            binding.yooMoneyTypes.clearChecked()
        }

        binding.yooMoneyAccept.isEnabled = state.acceptEnabled
    }

    private fun bindData(data: YooMoneyDialog) {
        binding.yooMoneyTitle.text = data.title

        if (data.help != null) {
            binding.yooMoneyHelp.text = data.help
            binding.yooMoneyTitle.setOnClickListener {
                viewModel.submitHelpClickAnalytics()
                binding.yooMoneyHelp.isVisible = !binding.yooMoneyHelp.isVisible
            }
        } else {
            binding.yooMoneyHelp.isVisible = false
            binding.yooMoneyTitle.setOnClickListener(null)
        }

        val amountViews = listOf<View>(
            binding.yooMoneyAmountTitle,
            binding.yooMoneyAmounts,
            binding.yooMoneyAmountInput
        )
        data.amounts.bindOptionalViews(amountViews) {
            binding.yooMoneyAmountTitle.text = it.title
            binding.yooMoneyAmountInput.hint = it.hint
            updateAmountsViews(it.items)
        }

        data.paymentTypes.also { types ->
            binding.yooMoneyTypeTitle.text = types.title

            val accountViews =
                listOf<View>(binding.yooMoneyTypeAccount, binding.yooMoneyTypeAccountName)
            val cardViews = listOf<View>(binding.yooMoneyTypeCard, binding.yooMoneyTypeCardName)
            val mobileViews =
                listOf<View>(binding.yooMoneyTypeMobile, binding.yooMoneyTypeMobileName)

            types.items
                .firstOrNull { it.id == YooMoneyDialog.TYPE_ID_ACCOUNT }
                .bindOptionalViews(accountViews) {
                    binding.yooMoneyTypeAccountName.text = it.title
                }
            types.items
                .firstOrNull { it.id == YooMoneyDialog.TYPE_ID_CARD }
                .bindOptionalViews(cardViews) {
                    binding.yooMoneyTypeCardName.text = it.title
                }
            types.items
                .firstOrNull { it.id == YooMoneyDialog.TYPE_ID_MOBILE }
                .bindOptionalViews(mobileViews) {
                    binding.yooMoneyTypeMobileName.text = it.title
                }

            val hasSupportedTypes = types.items.any {
                it.id == YooMoneyDialog.TYPE_ID_ACCOUNT ||
                        it.id == YooMoneyDialog.TYPE_ID_CARD ||
                        it.id == YooMoneyDialog.TYPE_ID_MOBILE
            }
            binding.yooMoneyTypeTitle.isVisible = hasSupportedTypes
            binding.yooMoneyTypes.isVisible = hasSupportedTypes
            binding.yooMoneyTypesNames.isVisible = hasSupportedTypes
        }

        binding.yooMoneyAccept.text = data.btDonateText
        binding.yooMoneyCancel.text = data.btCancelText
    }

    private fun updateAmountsViews(amounts: List<Int>) {
        if (binding.yooMoneyAmounts.childCount != amounts.size) {
            binding.yooMoneyAmounts.clearChecked()
            binding.yooMoneyAmounts.removeAllViews()

            amounts.forEach {
                val button = MaterialButton(
                    binding.yooMoneyAmounts.context,
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
                binding.yooMoneyAmounts.addView(button, layoutParams)
            }

        }
        amounts.forEachIndexed { index, amount ->
            (binding.yooMoneyAmounts.getChildAt(index) as MaterialButton?)?.also {
                it.id = amount
                it.text = amount.toString()
            }
        }
    }

    private fun TextView.getTextIntValue(): Int? = text?.toString()?.toIntOrNull()

    private fun MaterialButtonToggleGroup.addCheckedListener(action: (checkedId: Int?) -> Unit) {
        var lastValue: Int? = null
        addOnButtonCheckedListener { group, checkedId, isChecked ->
            val isSameValue = group.checkedButtonIds.size == 1
                    && group.checkedButtonIds[0] == group.checkedButtonId
            val isNoSelection = group.checkedButtonIds.isEmpty()
            if (isSameValue || isNoSelection) {
                val newValue = if (isNoSelection) null else group.checkedButtonId
                if (newValue != lastValue) {
                    lastValue = newValue
                    action.invoke(lastValue)
                }
            }
        }
    }

    private class AmountCurrencyTransformation : TransformationMethod {

        override fun getTransformation(source: CharSequence?, view: View?): CharSequence {
            return source?.toString().orEmpty() + " ₽"
        }

        override fun onFocusChanged(p0: View?, p1: CharSequence?, p2: Boolean, p3: Int, p4: Rect?) {
        }
    }
}