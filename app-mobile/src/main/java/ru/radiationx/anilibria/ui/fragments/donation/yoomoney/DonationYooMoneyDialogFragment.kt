package ru.radiationx.anilibria.ui.fragments.donation.yoomoney

import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.android.synthetic.main.dialog_donation_yoomoney.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.donation.infra.DonationYooMoneyState
import ru.radiationx.anilibria.presentation.donation.yoomoney.DonationYooMoneyPresenter
import ru.radiationx.anilibria.presentation.donation.yoomoney.DonationYooMoneyView
import ru.radiationx.anilibria.ui.fragments.AlertDialogFragment
import ru.radiationx.data.entity.app.donation.donate.DonationYooMoneyInfo
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
    }

    override fun showData(state: DonationYooMoneyState) {
        bindData(state.data)

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
            updateAmounts(it.items)
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
    }

    private fun updateAmounts(amounts: List<Int>) {
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
                yooMoneyAmounts.addView(button, layoutParams)
            }

        }
        amounts.forEachIndexed { index, amount ->
            (yooMoneyAmounts.getChildAt(index) as MaterialButton?)?.also {
                it.text = "$amount ₽"
                it.tag = amount
            }
        }
    }

    override fun setRefreshing(refreshing: Boolean) {
    }
}