package ru.radiationx.anilibria.ui.fragments.donation.jointeam

import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_donation_join_team.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.donation.jointeam.DonationJoinTeamPresenter
import ru.radiationx.anilibria.presentation.donation.jointeam.DonationJoinTeamView
import ru.radiationx.anilibria.ui.fragments.AlertDialogFragment
import ru.radiationx.anilibria.utils.LinkMovementMethod
import ru.radiationx.data.entity.app.donation.other.DonationJoinTeamInfo
import ru.radiationx.shared.ktx.android.bindOptionalView
import ru.radiationx.shared_app.di.getDependency

class DonationJoinTeamDialogFragment :
    AlertDialogFragment(R.layout.dialog_donation_join_team), DonationJoinTeamView {

    @InjectPresenter
    lateinit var presenter: DonationJoinTeamPresenter

    @ProvidePresenter
    fun providePresenter(): DonationJoinTeamPresenter =
        getDependency(DonationJoinTeamPresenter::class.java)

    override fun onStart() {
        super.onStart()
        getAlertDialog()?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        joinTeamDesc.movementMethod = LinkMovementMethod {
            presenter.onLinkClick(it)
            true
        }

        joinTeamNotice.setOnClickListener {
            presenter.onNoticeClick()
        }

        joinTeamTelegram.setOnClickListener {
            presenter.onTelegramClick()
            dismiss()
        }

        joinTeamCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun showData(data: DonationJoinTeamInfo) {
        joinTeamTitle.text = data.title
        joinTeamDesc.text = Html.fromHtml(data.desc)

        data.voicerNotice.bindOptionalView(joinTeamNoticeText) {
            joinTeamNoticeText.text = it
        }
        data.btVoicer.bindOptionalView(joinTeamNotice) {
            joinTeamNotice.text = it.text
        }
        data.btTelegram.bindOptionalView(joinTeamTelegram) {
            joinTeamTelegram.text = it.text
        }
        joinTeamCancel.text = data.btCancelText
    }

    override fun setRefreshing(refreshing: Boolean) {
    }
}