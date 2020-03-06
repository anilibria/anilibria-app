package ru.radiationx.anilibria.screen

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import ru.radiationx.anilibria.common.fragment.BaseGuidedStepFragment
import ru.terrakok.cicerone.Router
import java.util.*
import javax.inject.Inject

class DialogExampleFragment : BaseGuidedStepFragment() {

    companion object {
        private const val ACTION_ID_POSITIVE = 1
        private const val ACTION_ID_NEGATIVE = ACTION_ID_POSITIVE + 1
        private const val ACTION_ID_HD = ACTION_ID_NEGATIVE + 1
    }

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
                .id(ACTION_ID_NEGATIVE.toLong())
                .title("Пропустить")
                .build()
        )
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        if (ACTION_ID_POSITIVE.toLong() == action.id) {

            router.replaceScreen(TestGuidedStepScreen())
        } else if (ACTION_ID_HD.toLong() == action.id) {
            router.exit()
        } else {
            router.navigateTo(TestGuidedStepScreen())
        }
    }
}