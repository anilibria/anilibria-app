package ru.radiationx.anilibria.screen.auth.main

import android.os.Bundle
import android.util.Log
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import ru.radiationx.anilibria.common.fragment.scoped.ScopedGuidedStepFragment
import ru.radiationx.shared_app.di.viewModel

class AuthGuidedFragment : ScopedGuidedStepFragment() {

    companion object {
        private const val CODE_ACTION_ID = 1L
        private const val CLASSIC_ACTION_ID = 2L
        private const val SOCIAL_ACTION_ID = 3L
        private const val SKIP_ACTION_ID = 4L
    }

    private val viewModel by viewModel<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
    }

    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance = Guidance(
        "Авторизация",
        "Войдите в свой аккаунт удобным для вас способом. \nДля регистрации используйте полную версию сайта.",
        null,
        null
    )

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(CODE_ACTION_ID)
                .title("Войти по коду")
                .description("Используйте мобильное приложение или сайт")
                .build()
        )

        actions.add(
            GuidedAction.Builder(requireContext())
                .id(CLASSIC_ACTION_ID)
                .title("Ввести логин или email")
                .build()
        )

        /*actions.add(
            GuidedAction.Builder(requireContext())
                .id(SOCIAL_ACTION_ID)
                .title("Войти через ВКонтакте")
                .build()
        )*/

        actions.add(
            GuidedAction.Builder(requireContext())
                .id(SKIP_ACTION_ID)
                .title("Пропустить")
                .build()
        )
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            CODE_ACTION_ID -> viewModel.onCodeClick()
            CLASSIC_ACTION_ID -> viewModel.onClassicClick()
            SOCIAL_ACTION_ID -> viewModel.onSocialClick()
            SKIP_ACTION_ID -> viewModel.onSkipClick()
        }
    }
}