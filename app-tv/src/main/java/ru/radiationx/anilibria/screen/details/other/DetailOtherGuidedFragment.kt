package ru.radiationx.anilibria.screen.details.other

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.anilibria.screen.details.DetailExtra
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra

class DetailOtherGuidedFragment : FakeGuidedStepFragment() {

    companion object {
        private const val CLEAR_ACTION_ID = 0L
        private const val MARK_ACTION_ID = 1L
        private const val ARG_ID = "id"

        fun newInstance(releaseId: ReleaseId) = DetailOtherGuidedFragment().putExtra {
            putParcelable(ARG_ID, releaseId)
        }
    }

    private val viewModel by viewModel<DetailOtherViewModel> {
        DetailExtra(getExtraNotNull(ARG_ID))
    }

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(CLEAR_ACTION_ID)
                .title("Сбросить историю просмотров")
                .build()
        )
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(MARK_ACTION_ID)
                .title("Отметить всё как просмотренные")
                .build()
        )
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        super.onGuidedActionClicked(action)
        when (action.id) {
            CLEAR_ACTION_ID -> viewModel.onClearClick()
            MARK_ACTION_ID -> viewModel.onMarkClick()
        }
    }
}