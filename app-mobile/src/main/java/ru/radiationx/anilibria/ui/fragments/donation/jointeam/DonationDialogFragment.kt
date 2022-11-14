package ru.radiationx.anilibria.ui.fragments.donation.jointeam

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.DialogDonationContentBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.presentation.donation.jointeam.DonationDialogPresenter
import ru.radiationx.anilibria.presentation.donation.jointeam.DonationJoinTeamView
import ru.radiationx.anilibria.ui.fragments.AlertDialogFragment
import ru.radiationx.anilibria.ui.fragments.donation.adapter.DonationContentAdapter
import ru.radiationx.data.entity.domain.donation.DonationDialog
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared_app.di.getDependency

class DonationDialogFragment :
    AlertDialogFragment(R.layout.dialog_donation_content), DonationJoinTeamView {

    companion object {
        private const val ARG_TAG = "arg_tag"

        fun newInstance(tag: String) = DonationDialogFragment().putExtra {
            putString(ARG_TAG, tag)
        }
    }

    private val contentAdapter = DonationContentAdapter(
        buttonClickListener = {
            presenter.onButtonClick(it)
            dismiss()
        },
        linkClickListener = { presenter.onLinkClick(it) }
    )

    @InjectPresenter
    lateinit var presenter: DonationDialogPresenter

    @ProvidePresenter
    fun providePresenter(): DonationDialogPresenter =
        getDependency(DonationDialogPresenter::class.java)

    private val binding by viewBinding<DialogDonationContentBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.argTag = arguments?.getString(ARG_TAG, presenter.argTag)
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
    }

    override fun showData(data: DonationDialog) {
        contentAdapter.bindState(data.content)
        binding.btCancel.text = data.cancelText
        binding.btCancel.isVisible = data.cancelText != null
    }
}