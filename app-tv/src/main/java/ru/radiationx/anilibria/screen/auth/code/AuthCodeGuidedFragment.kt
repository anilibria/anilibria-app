package ru.radiationx.anilibria.screen.auth.code

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import androidx.leanback.widget.GuidedActionsStylist
import ru.radiationx.anilibria.common.fragment.scoped.ScopedGuidedStepFragment
import ru.radiationx.anilibria.screen.auth.GuidedProgressAction
import ru.radiationx.anilibria.screen.auth.GuidedProgressActionsStylist
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModel

class AuthCodeGuidedFragment : ScopedGuidedStepFragment() {

    companion object {
        private const val COMPLETE_ACTION_ID = 1L
        private const val EXPIRED_ACTION_ID = 2L
        private const val REPEAT_ACTION_ID = 3L
    }

    private val completeAction by lazy {
        GuidedProgressAction.Builder(requireContext())
            .id(COMPLETE_ACTION_ID)
            .title("Готово")
            .description("Нажмите, когда введёте код")
            .build()
    }

    private val expiredAction by lazy {
        GuidedProgressAction.Builder(requireContext())
            .id(EXPIRED_ACTION_ID)
            .title("Показать новый код")
            .description("Время действия текущего кода истекло")
            .build()
    }

    private val repeatAction by lazy {
        GuidedProgressAction.Builder(requireContext())
            .id(REPEAT_ACTION_ID)
            .title("Повторить")
            .description("Произошла ошибка")
            .build()
    }

    private val viewModel by viewModel<AuthCodeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeTo(viewModel.otpInfoData) {
            guidanceStylist.apply {
                titleView.text = "Код: ${it.code}"
                descriptionView.text = it.description
            }
        }

        subscribeTo(viewModel.state) {
            Log.e("lalala", "State $it, ${it.progress}")
            val primaryAction = when (it.buttonState) {
                AuthCodeViewModel.ButtonState.COMPLETE -> completeAction
                AuthCodeViewModel.ButtonState.EXPIRED -> expiredAction
                AuthCodeViewModel.ButtonState.REPEAT -> repeatAction
            }

            actions = if (it.error.isEmpty()) {
                listOf(primaryAction)
            } else {
                val errorAction = GuidedAction.Builder(requireContext())
                    .title("Ошибка")
                    .multilineDescription(true)
                    .description(it.error)
                    .infoOnly(true)
                    .focusable(false)
                    .build()
                listOf(primaryAction, errorAction)
            }
            primaryAction.updateProgress(it.progress)
        }
    }

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance = GuidanceStylist.Guidance(
        "Запрашивается код",
        null,
        "Авторизация",
        null
    )

    override fun onCreateActionsStylist(): GuidedActionsStylist {
        return GuidedProgressActionsStylist()
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            COMPLETE_ACTION_ID -> viewModel.onCompleteClick()
            EXPIRED_ACTION_ID -> viewModel.onExpiredClick()
            REPEAT_ACTION_ID -> viewModel.onRepeatClick()
        }
    }

    private fun GuidedProgressAction.updateProgress(progress: Boolean) {
        updateAction {
            showProgress = progress
            isEnabled = !progress
        }
    }

    private fun <T : GuidedAction> T.updateAction(block: T.() -> Unit) {
        findButtonActionById(id)?.apply {
            block()
            notifyButtonActionChanged(findButtonActionPositionById(id))
        }
        findActionById(id)?.apply {
            block()
            notifyActionChanged(findActionPositionById(id))
        }
    }
}