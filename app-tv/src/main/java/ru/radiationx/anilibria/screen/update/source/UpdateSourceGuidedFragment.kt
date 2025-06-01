package ru.radiationx.anilibria.screen.update.source

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.data.app.updater.models.UpdateData
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.subscribeTo

class UpdateSourceGuidedFragment : FakeGuidedStepFragment() {

    private val viewModel by viewModel<UpdateSourceViewModel>()

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        subscribeTo(viewModel.sourcesData) {
            actions = it.mapIndexed { index: Int, updateLink: UpdateData.UpdateLink ->
                GuidedAction.Builder(requireContext())
                    .id(index.toLong())
                    .title(updateLink.name)
                    .build()
            }
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        viewModel.onLinkClick(action.id.toInt())
    }
}