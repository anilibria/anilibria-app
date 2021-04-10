package ru.radiationx.anilibria.ui.fragments.donation.detail

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.fragment_donation_detail.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.auth.otp.OtpAcceptPresenter
import ru.radiationx.anilibria.presentation.donation.detail.DonationDetailPresenter
import ru.radiationx.anilibria.presentation.donation.detail.DonationDetailView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.data.entity.app.donation.DonationDetail
import ru.radiationx.shared.ktx.android.bindOptionalView
import ru.radiationx.shared.ktx.android.bindOptionalViews
import ru.radiationx.shared_app.di.getDependency

class DonationDetailFragment : BaseFragment(), DonationDetailView {

    @InjectPresenter
    lateinit var presenter: DonationDetailPresenter

    @ProvidePresenter
    fun provideAuthPresenter(): DonationDetailPresenter =
        getDependency(DonationDetailPresenter::class.java)

    override fun getBaseLayout(): Int = R.layout.fragment_donation_detail

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        donationToolbar.setNavigationOnClickListener {
            presenter.onBackPressed()
        }

        donationPatreon.setOnClickListener {
            presenter.onPatreonClick()
        }

        donationYooMoney.setOnClickListener {
            presenter.onYooMoneyClick()
        }

        donationDonationAlerts.setOnClickListener {
            presenter.onDonationAlertsClick()
        }

        donationJoinTeam.setOnClickListener {
            presenter.onJoinTeamClick()
        }

        donationInfra.setOnClickListener {
            presenter.onInfraClick()
        }
    }

    override fun showData(donationDetail: DonationDetail) {
        donationTitle.text = donationDetail.title

        val goodViews = listOf<View>(donationGoodTitle, donationGoodDesc)
        val badViews = listOf<View>(donationBadTitle, donationBadDesc)
        val donateViews = listOf<View>(
            donationDonateTitle,
            donationPatreon,
            donationYooMoney,
            donationDonationAlerts
        )
        val otherViews = listOf<View>(
            donationOtherTitle,
            donationJoinTeam,
            donationInfra
        )

        donationDetail.good.bindOptionalViews(goodViews) {
            donationGoodTitle.text = it.title
            donationGoodDesc.text = it.desc
        }

        donationDetail.bad.bindOptionalViews(badViews) {
            donationBadTitle.text = it.title
            donationBadDesc.text = it.desc
        }

        donationDetail.donateSupport.bindOptionalViews(donateViews) { support ->
            donationDonateTitle.text = support.title

            support.btPatreon.bindOptionalView(donationPatreon) {
                donationPatreon.text = it.text
            }
            support.btYooMoney.bindOptionalView(donationYooMoney) {
                donationYooMoney.text = it.text
            }
            support.btDonationAlerts.bindOptionalView(donationDonationAlerts) {
                donationDonationAlerts.text = it.text
            }
        }

        donationDetail.otherSupport.bindOptionalViews(otherViews) { support ->
            donationOtherTitle.text = support.title

            support.btJoinTeam.bindOptionalView(donationJoinTeam) {
                donationJoinTeam.text = it.text
            }
            support.btInfra.bindOptionalView(donationInfra) {
                donationInfra.text = it.text
            }
        }

        donationDetail.footerText.bindOptionalView(donationFooterText) {
            donationFooterText.text = it
        }
    }

    override fun setRefreshing(refreshing: Boolean) {

    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

}