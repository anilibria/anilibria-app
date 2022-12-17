package ru.radiationx.anilibria.screen.search

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.GuidedAction
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.anilibria.screen.search.BaseSearchValuesGuidedFragment.Companion.ARG_VALUES
import ru.radiationx.anilibria.ui.widget.manager.ExternalProgressManager
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.subscribeTo

data class SearchValuesExtra(
    val values: List<String>
) : QuillExtra

abstract class BaseSearchValuesGuidedFragment : FakeGuidedStepFragment() {

    companion object {
        const val ARG_VALUES = "arg values"
    }

    protected val argExtra by lazy {
        SearchValuesExtra(getExtraNotNull(ARG_VALUES))
    }

    private val progressManager by lazy { ExternalProgressManager() }

    protected abstract val viewModel: BaseSearchValuesViewModel

    override fun onProvideTheme(): Int = R.style.AppTheme_Player_LeanbackWizard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        progressManager.rootView =
            view.findViewById<View>(androidx.leanback.R.id.action_fragment_root) as? ViewGroup

        subscribeTo(viewModel.progressState) {
            if (it) {
                progressManager.show()
            } else {
                progressManager.hide()
            }
        }

        subscribeTo(viewModel.valuesData) {
            actions = it.mapIndexed { index: Int, title: String ->
                GuidedAction.Builder(requireContext())
                    .id(index.toLong())
                    .title(title)
                    .checkSetId(GuidedAction.CHECKBOX_CHECK_SET_ID)
                    .build()
            }
        }

        subscribeTo(viewModel.checkedIndicesData) {
            it.forEach { pair ->
                val action = findActionById(pair.first.toLong())
                if (action.isChecked != pair.second) {
                    action.isChecked = pair.second
                    notifyActionChanged(findActionPositionById(action.id))
                }
            }
        }

        subscribeTo(viewModel.selectedIndex) {
            selectedActionPosition = it
        }
    }

    override fun onCreateButtonActions(
        actions: MutableList<GuidedAction>,
        savedInstanceState: Bundle?
    ) {
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(GuidedAction.ACTION_ID_OK)
                .title("Ок")
                .build()
        )

        actions.add(
            GuidedAction.Builder(requireContext())
                .id(GuidedAction.ACTION_ID_CANCEL)
                .title("Сбросить")
                .build()
        )
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            GuidedAction.ACTION_ID_OK -> viewModel.applyValues()
            GuidedAction.ACTION_ID_CANCEL -> viewModel.resetSelected()
            else -> viewModel.setSelected(action.id.toInt(), action.isChecked)
        }
    }
}

fun <T : BaseSearchValuesGuidedFragment> T.putValues(values: List<String>): T = putExtra {
    putStringArrayList(ARG_VALUES, ArrayList(values))
}