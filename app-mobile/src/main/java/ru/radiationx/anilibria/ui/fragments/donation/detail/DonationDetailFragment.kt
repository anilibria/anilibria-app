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
import ru.radiationx.anilibria.ui.fragments.BaseDimensionsFragment
import ru.radiationx.anilibria.ui.fragments.donation.adapter.DonationContentAdapter
import ru.radiationx.anilibria.ui.fragments.donation.jointeam.DonationDialogFragment
import ru.radiationx.anilibria.ui.fragments.donation.yoomoney.DonationYooMoneyDialogFragment
import ru.radiationx.anilibria.utils.Dimensions
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.launchInResumed
import kotlin.math.roundToInt

class DonationDetailFragment : BaseDimensionsFragment(R.layout.fragment_donation_detail) {

    private val contentAdapter = DonationContentAdapter(
        buttonClickListener = { viewModel.onButtonClick(it) },
        linkClickListener = { viewModel.onLinkClick(it) }
    )

    private val binding by viewBinding<FragmentDonationDetailBinding>()

    private val viewModel by viewModel<DonationDetailViewModel>()

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
        }.launchInResumed(viewLifecycleOwner)


        viewModel.dialogEvent.onEach { tag ->
            DonationDialogFragment.newInstance(tag).show(childFragmentManager, tag)
        }.launchInResumed(viewLifecycleOwner)
    }

    override fun updateDimens(dimensions: Dimensions) {
        super.updateDimens(dimensions)
        binding.donationToolbar.updatePadding(top = dimensions.insets.top)
        binding.donationToolbar.doOnNextLayout {
            binding.donationRecycler.updatePadding(top = it.height + (16 * binding.donationRecycler.resources.displayMetrics.density).roundToInt())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.donationRecycler.adapter = null
    }
}