package ru.radiationx.anilibria.ui.fragments.donation.jointeam

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.DialogDonationContentBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.presentation.donation.jointeam.DonationDialogViewModel
import ru.radiationx.anilibria.ui.fragments.AlertDialogFragment
import ru.radiationx.anilibria.ui.fragments.donation.adapter.DonationContentAdapter
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared_app.di.viewModel

class DonationDialogFragment : AlertDialogFragment(R.layout.dialog_donation_content) {

    companion object {
        private const val ARG_TAG = "arg_tag"

        fun newInstance(tag: String) = DonationDialogFragment().putExtra {
            putString(ARG_TAG, tag)
        }
    }

    private val contentAdapter = DonationContentAdapter(
        buttonClickListener = {
            viewModel.onButtonClick(it)
            dismiss()
        },
        linkClickListener = { viewModel.onLinkClick(it) }
    )

    private val binding by viewBinding<DialogDonationContentBinding>()

    private val viewModel by viewModel<DonationDialogViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.argTag = arguments?.getString(ARG_TAG, viewModel.argTag)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.contentRecycler.apply {
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context)
            disableItemChangeAnimation()
        }

        binding.btCancel.setOnClickListener {
            dismiss()
        }

        viewModel.state.onEach { data ->
            contentAdapter.bindState(data.content)
            binding.btCancel.text = data.cancelText
            binding.btCancel.isVisible = data.cancelText != null
        }
    }
}