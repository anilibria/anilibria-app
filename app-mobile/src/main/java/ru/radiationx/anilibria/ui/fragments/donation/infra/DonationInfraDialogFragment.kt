package ru.radiationx.anilibria.ui.fragments.donation.infra

import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_donation_infra.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.donation.infra.DonationInfraPresenter
import ru.radiationx.anilibria.presentation.donation.infra.DonationInfraView
import ru.radiationx.anilibria.ui.fragments.AlertDialogFragment
import ru.radiationx.anilibria.utils.LinkMovementMethod
import ru.radiationx.data.entity.app.donation.other.DonationInfraInfo
import ru.radiationx.shared.ktx.android.bindOptionalView
import ru.radiationx.shared_app.di.getDependency

class DonationInfraDialogFragment :
    AlertDialogFragment(R.layout.dialog_donation_infra), DonationInfraView {

    @InjectPresenter
    lateinit var presenter: DonationInfraPresenter

    @ProvidePresenter
    fun providePresenter(): DonationInfraPresenter =
        getDependency(DonationInfraPresenter::class.java)

    override fun onStart() {
        super.onStart()
        getAlertDialog()?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        infraDesc.movementMethod = LinkMovementMethod {
            presenter.onLinkClick(it)
            true
        }

        infraTelegram.setOnClickListener {
            presenter.onTelegramClick()
            dismiss()
        }

        infraCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun showData(data: DonationInfraInfo) {
        infraTitle.text = data.title
        infraDesc.text = Html.fromHtml(data.description)
        data.btTelegram.bindOptionalView(infraTelegram) {
            infraTelegram.text = it.text
        }
        infraCancel.text = data.btCancelText
    }

    override fun setRefreshing(refreshing: Boolean) {
    }
}