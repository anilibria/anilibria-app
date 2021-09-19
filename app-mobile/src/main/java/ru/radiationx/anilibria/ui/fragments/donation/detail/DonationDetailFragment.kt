package ru.radiationx.anilibria.ui.fragments.donation.detail

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePadding
import androidx.transition.AutoTransition
import kotlinx.android.synthetic.main.fragment_donation_detail.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.donation.detail.DonationDetailPresenter
import ru.radiationx.anilibria.presentation.donation.detail.DonationDetailView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.donation.infra.DonationInfraDialogFragment
import ru.radiationx.anilibria.ui.fragments.donation.jointeam.DonationJoinTeamDialogFragment
import ru.radiationx.anilibria.ui.fragments.donation.yoomoney.DonationYooMoneyDialogFragment
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.anilibria.utils.LinkMovementMethod
import ru.radiationx.data.entity.app.donation.DonationDetailResponse
import ru.radiationx.shared.ktx.android.bindOptionalView
import ru.radiationx.shared.ktx.android.bindOptionalViews

class DonationDetailFragment : BaseFragment(), DonationDetailView {

    @InjectPresenter
    lateinit var presenter: DonationDetailPresenter

    @ProvidePresenter
    fun provideAuthPresenter(): DonationDetailPresenter =
        getDependency(DonationDetailPresenter::class.java, screenScope)

    override fun getBaseLayout(): Int = R.layout.fragment_donation_detail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = AutoTransition()
        exitTransition = AutoTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val linkMovementMethod = LinkMovementMethod {
            presenter.onLinkClick(it)
            true
        }

        donationGoodDesc.movementMethod = linkMovementMethod
        donationBadDesc.movementMethod = linkMovementMethod

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

    override fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        super.updateDimens(dimensions)
        donationToolbar.updatePadding(top = dimensions.statusBar)
        donationToolbar.doOnNextLayout {
            donationScrollView.updatePadding(top = it.height)
        }
    }

    override fun showData(data: DonationDetailResponse) {
        donationTitle.text = data.title

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

        data.good.bindOptionalViews(goodViews) {
            donationGoodTitle.text = it.title
            donationGoodDesc.text = Html.fromHtml(it.desc)
        }

        data.bad.bindOptionalViews(badViews) {
            donationBadTitle.text = it.title
            donationBadDesc.text = Html.fromHtml(it.desc)
        }

        data.donateSupport.bindOptionalViews(donateViews) { support ->
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

        data.otherSupport.bindOptionalViews(otherViews) { support ->
            donationOtherTitle.text = support.title

            support.btJoinTeam.bindOptionalView(donationJoinTeam) {
                donationJoinTeam.text = it.text
            }
            support.btInfra.bindOptionalView(donationInfra) {
                donationInfra.text = it.text
            }
        }

        data.footerText.bindOptionalView(donationFooterText) {
            donationFooterText.text = Html.fromHtml(it)
        }
    }

    override fun openYooMoney() {
        DonationYooMoneyDialogFragment().show(childFragmentManager, "yoomoney")
    }

    override fun openJoinTeam() {
        DonationJoinTeamDialogFragment().show(childFragmentManager, "jointeam")
    }

    override fun openInfra() {
        DonationInfraDialogFragment().show(childFragmentManager, "infra")
    }

    override fun setRefreshing(refreshing: Boolean) {

    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

}