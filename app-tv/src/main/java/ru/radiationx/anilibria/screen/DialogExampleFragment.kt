package ru.radiationx.anilibria.screen

import android.os.Bundle
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import ru.radiationx.anilibria.common.fragment.scoped.ScopedGuidedStepFragment
import ru.radiationx.anilibria.common.fragment.DialogRouter
import ru.terrakok.cicerone.Router
import java.util.*
import javax.inject.Inject

class DialogExampleFragment : ScopedGuidedStepFragment() {

    companion object {
        private const val ACTION_ID_POSITIVE = 1
        private const val ACTION_ID_NEGATIVE = ACTION_ID_POSITIVE + 1
        private const val ACTION_ID_HD = ACTION_ID_NEGATIVE + 1
        private const val ACTION_ID_SD = ACTION_ID_HD + 1
    }

    @Inject
    lateinit var dialogRouter: DialogRouter

    @Inject
    lateinit var router: Router

    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance = Guidance(
        "Авторизация",
        "Войдите в свой аккаунт удобным для вас способом. \nДля регистрации используйте полную версию сайта.",
        Date().toString(),
        null
    )

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(ACTION_ID_POSITIVE.toLong())
                .title("Войти по коду")
                .description("Используйте мобильное приложение или сайт")
                .build()
        )

        actions.add(
            GuidedAction.Builder(requireContext())
                .id(ACTION_ID_NEGATIVE.toLong())
                .title("Ввести логин или email")
                .build()
        )

        actions.add(
            GuidedAction.Builder(requireContext())
                .id(ACTION_ID_HD.toLong())
                .title("Войти через ВКонтакте")
                .build()
        )

        actions.add(
            GuidedAction.Builder(requireContext())
                .id(ACTION_ID_SD.toLong())
                .title("Пропустить")
                .build()
        )
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_ID_POSITIVE.toLong() -> {
                dialogRouter.replaceDialog(TestGuidedStepScreen())
            }
            ACTION_ID_HD.toLong() -> {
                dialogRouter.backDialog()
            }
            ACTION_ID_SD.toLong() -> {
                dialogRouter.finishDialogChain()
                router.navigateTo(TestScreen())
            }
            else -> {
                dialogRouter.navigateTo(TestGuidedStepScreen())
            }
        }
    }
}