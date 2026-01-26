package ru.radiationx.anilibria.screen.update.warning

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.quill.QuillExtra
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra

data class UpdateWarningExtra(
    val link: UpdateData.UpdateLink
) : QuillExtra

class UpdateWarningGuidedFragment : FakeGuidedStepFragment() {

    companion object {
        private const val ARG_LINK = "arg_link"

        fun newInstance(link: UpdateData.UpdateLink) = UpdateWarningGuidedFragment().putExtra {
            putParcelable(ARG_LINK, link)
        }
    }

    private val viewModel by viewModel<UpdateWarningViewModel> {
        UpdateWarningExtra(getExtraNotNull(ARG_LINK))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        actions = listOf(
            GuidedAction.Builder(requireContext())
                .id(UpdateWarningViewModel.ACTION_OPEN_STORE)
                .title("Открыть в приложении RuStore")
                .build(),
            GuidedAction.Builder(requireContext())
                .id(UpdateWarningViewModel.ACTION_OPEN_STORE_SITE)
                .title("Открыть на сайте RuStore")
                .build(),
            GuidedAction.Builder(requireContext())
                .id(UpdateWarningViewModel.ACTION_OPEN_FILE)
                .title("Открыть ссылку на файл")
                .build(),
        )
    }


    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance =
        GuidanceStylist.Guidance(
            "Предупреждение",
            "Установка файлов запрещена в версии для RuStore",
            "Обновление приложения",
            null
        )

    override fun onGuidedActionClicked(action: GuidedAction) {
        viewModel.onLinkClick(action.id)
    }
}