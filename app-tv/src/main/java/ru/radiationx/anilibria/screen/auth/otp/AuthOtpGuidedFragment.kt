package ru.radiationx.anilibria.screen.auth.otp

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import androidx.leanback.widget.GuidedActionsStylist
import kotlinx.coroutines.flow.filterNotNull
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.anilibria.screen.auth.GuidedProgressAction
import ru.radiationx.anilibria.screen.auth.GuidedProgressActionsStylist
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.subscribeTo

class AuthOtpGuidedFragment : FakeGuidedStepFragment() {

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

    private val viewModel by viewModel<AuthOtpViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        subscribeTo(viewModel.otpInfoData.filterNotNull()) {
            guidanceStylist.apply {
                titleView?.text = "Код: ${it.code}"
                descriptionView?.text = buildString {
                    appendLine("1. Откройте в мобильном приложении свой профиль.")
                    appendLine("2. Нажмите 'привязать устройство'")
                    appendLine("3. Введите код")
                }
            }
        }

        subscribeTo(viewModel.state) {
            val primaryAction = when (it.buttonState) {
                AuthOtpViewModel.ButtonState.COMPLETE -> completeAction
                AuthOtpViewModel.ButtonState.EXPIRED -> expiredAction
                AuthOtpViewModel.ButtonState.REPEAT -> repeatAction
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

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance =
        GuidanceStylist.Guidance(
            "Запрашивается код",
            "Запрашивается код",
            "Авторизация",
            null
        )

    override fun onCreateActionsStylist(): GuidedActionsStylist = GuidedProgressActionsStylist()

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            COMPLETE_ACTION_ID -> viewModel.onCompleteClick()
            EXPIRED_ACTION_ID -> viewModel.onExpiredClick()
            REPEAT_ACTION_ID -> viewModel.onRepeatClick()
        }
    }


    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)
        actions.add(completeAction)
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