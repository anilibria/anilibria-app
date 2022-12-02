package ru.radiationx.anilibria.ui.fragments.donation.detail

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentDonationDetailBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.presentation.donation.detail.DonationDetailViewModel
import ru.radiationx.anilibria.ui.fragments.ScopeFragment
import ru.radiationx.anilibria.ui.fragments.donation.adapter.DonationContentAdapter
import ru.radiationx.anilibria.ui.fragments.donation.jointeam.DonationDialogFragment
import ru.radiationx.anilibria.ui.fragments.donation.yoomoney.DonationYooMoneyDialogFragment
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.quill.quillViewModel
import kotlin.math.roundToInt

class DonationDetailFragment : ScopeFragment(R.layout.fragment_donation_detail) {

    private val contentAdapter = DonationContentAdapter(
        buttonClickListener = { viewModel.onButtonClick(it) },
        linkClickListener = { viewModel.onLinkClick(it) }
    )

    private val binding by viewBinding<FragmentDonationDetailBinding>()

    private val viewModel by quillViewModel<DonationDetailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = AutoTransition()
        exitTransition = AutoTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.donationToolbar.setNavigationOnClickListener { viewModel.onBackPressed() }

        binding.donationRecycler.apply {
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context)
            disableItemChangeAnimation()
        }

        viewModel.state.onEach { data ->
            contentAdapter.bindState(data.detailContent)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.yoomoneyEvent.onEach {
            DonationYooMoneyDialogFragment().show(childFragmentManager, "yoomoney")
        }.launchIn(viewLifecycleOwner.lifecycleScope)


        viewModel.dialogEvent.onEach { tag ->
            DonationDialogFragment.newInstance(tag).show(childFragmentManager, tag)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        super.updateDimens(dimensions)
        binding.donationToolbar.updatePadding(top = dimensions.statusBar)
        binding.donationToolbar.doOnNextLayout {
            binding.donationRecycler.updatePadding(top = it.height + (16 * binding.donationRecycler.resources.displayMetrics.density).roundToInt())
        }
    }

    override fun onBackPressed(): Boolean {
        return false
    }

}