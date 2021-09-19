package ru.radiationx.anilibria.ui.fragments.donation.detail

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import kotlinx.android.synthetic.main.fragment_donation_detail.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.presentation.donation.detail.DonationDetailPresenter
import ru.radiationx.anilibria.presentation.donation.detail.DonationDetailView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.donation.adapter.DonationContentAdapter
import ru.radiationx.anilibria.ui.fragments.donation.jointeam.DonationDialogFragment
import ru.radiationx.anilibria.ui.fragments.donation.yoomoney.DonationYooMoneyDialogFragment
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.data.entity.domain.donation.DonationInfo
import kotlin.math.roundToInt

class DonationDetailFragment : BaseFragment(), DonationDetailView {

    @InjectPresenter
    lateinit var presenter: DonationDetailPresenter

    @ProvidePresenter
    fun provideAuthPresenter(): DonationDetailPresenter =
        getDependency(DonationDetailPresenter::class.java, screenScope)

    private val contentAdapter = DonationContentAdapter(
        buttonClickListener = { presenter.onButtonClick(it) },
        linkClickListener = { presenter.onLinkClick(it) }
    )

    override fun getBaseLayout(): Int = R.layout.fragment_donation_detail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = AutoTransition()
        exitTransition = AutoTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        donationRecycler.apply {
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context)
            disableItemChangeAnimation()
        }
    }

    override fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        super.updateDimens(dimensions)
        donationToolbar.updatePadding(top = dimensions.statusBar)
        donationToolbar.doOnNextLayout {
            donationRecycler.updatePadding(top = it.height + (16 * donationRecycler.resources.displayMetrics.density).roundToInt())
        }
    }

    override fun showData(data: DonationInfo) {
        contentAdapter.bindState(data.detailContent)
    }

    override fun openYooMoney() {
        DonationYooMoneyDialogFragment().show(childFragmentManager, "yoomoney")
    }

    override fun openContentDialog(tag: String) {
        DonationDialogFragment.newInstance(tag).show(childFragmentManager, tag)
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

}