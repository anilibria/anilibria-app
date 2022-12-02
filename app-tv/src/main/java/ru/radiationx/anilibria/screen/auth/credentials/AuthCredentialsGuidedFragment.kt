package ru.radiationx.anilibria.screen.auth.credentials

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.core.text.isDigitsOnly
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import androidx.leanback.widget.GuidedActionsStylist
import ru.radiationx.anilibria.common.fragment.FakeGuidedStepFragment
import ru.radiationx.anilibria.screen.auth.GuidedProgressAction
import ru.radiationx.anilibria.screen.auth.GuidedProgressActionsStylist
import ru.radiationx.quill.quillViewModel
import ru.radiationx.shared.ktx.android.subscribeTo

class AuthCredentialsGuidedFragment : FakeGuidedStepFragment() {

    companion object {
        private const val LOGIN_FIELD_ACTION_ID = 1L
        private const val PASSWORD_FIELD_ACTION_ID = 2L
        private const val CODE_FIELD_ACTION_ID = 3L
        private const val LOGIN_ACTION_ID = 4L
    }

    private val viewModel by quillViewModel<AuthCredentialsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeTo(viewModel.progressState) {
            updateEnabled(LOGIN_FIELD_ACTION_ID, !it)
            updateEnabled(PASSWORD_FIELD_ACTION_ID, !it)
            updateEnabled(CODE_FIELD_ACTION_ID, !it)
            updateEnabled(LOGIN_ACTION_ID, !it)
            updateProgress(it)
        }

        val defaultActions = actions.toList()

        subscribeTo(viewModel.error) { errorText ->
            actions = if (errorText.isEmpty()) {
                defaultActions
            } else {
                val errorAction = GuidedAction.Builder(requireContext())
                    .title("Ошибка")
                    .multilineDescription(true)
                    .description(errorText)
                    .infoOnly(true)
                    .focusable(false)
                    .build()
                defaultActions + errorAction
            }
        }
    }

    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance = Guidance(
        "Авторизация",
        "Логин и пароль обязательны для заполнения.\nЕсли вы не настраивали двухфакторную авторизацию, то код вводить не нужно.",
        null,
        null
    )

    override fun onCreateButtonActionsStylist(): GuidedActionsStylist =
        GuidedProgressActionsStylist().apply {
            setAsButtonActions()
        }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(LOGIN_FIELD_ACTION_ID)
                .title("Логин или email")
                .description("Введите логин или email")
                .editDescription("")
                .descriptionEditable(true)
                .build()
        )
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(PASSWORD_FIELD_ACTION_ID)
                .title("Пароль")
                .description("Введите пароль")
                .editDescription("")
                .descriptionEditable(true)
                .descriptionEditInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .build()
        )
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(CODE_FIELD_ACTION_ID)
                .title("Код двухфакторной авторизации")
                .description("Оставьте поле пустым, если у вас не настроена двухфакторная авторизация")
                .multilineDescription(true)
                .editDescription("")
                .descriptionEditable(true)
                .descriptionEditInputType(InputType.TYPE_CLASS_NUMBER)
                .build()
        )
    }

    override fun onCreateButtonActions(
        actions: MutableList<GuidedAction>,
        savedInstanceState: Bundle?
    ) {
        actions.add(
            GuidedProgressAction.Builder(requireContext())
                .id(LOGIN_ACTION_ID)
                .title("Войти")
                .build()
                .apply {
                    isEnabled = false
                }
        )
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            LOGIN_ACTION_ID -> {
                val login = getFieldValue(LOGIN_FIELD_ACTION_ID)
                val password = getFieldValue(PASSWORD_FIELD_ACTION_ID)
                val code = getFieldValue(CODE_FIELD_ACTION_ID)
                viewModel.onLoginClicked(login, password, code)
            }
        }
    }

    override fun onGuidedActionEditCanceled(action: GuidedAction) {
        validateAction(action)
    }

    override fun onGuidedActionEditedAndProceed(action: GuidedAction): Long {
        return validateAction(action)
    }

    private fun validateAction(action: GuidedAction): Long {
        val value = action.editDescription?.toString().orEmpty()

        val loginValid = validateLogin()
        val passwordValid = validatePassword()
        val codeValid = validateCode()

        updateEnabled(LOGIN_ACTION_ID, loginValid && passwordValid && codeValid)

        when (action.id) {

            LOGIN_FIELD_ACTION_ID -> {
                if (loginValid) {
                    action.description = value
                } else {
                    action.description = "Поле заполнено неверно"
                    return GuidedAction.ACTION_ID_CURRENT
                }
            }

            PASSWORD_FIELD_ACTION_ID -> {
                if (passwordValid) {
                    action.description = "Поле заполнено верно"
                } else {
                    action.description = "Поле заполнено неверно"
                    return GuidedAction.ACTION_ID_CURRENT
                }
            }

            CODE_FIELD_ACTION_ID -> {
                if (codeValid) {
                    action.description = value
                } else {
                    action.description = "Поле заполнено неверно"
                    return GuidedAction.ACTION_ID_CURRENT
                }
            }
        }

        return GuidedAction.ACTION_ID_NEXT
    }

    private fun validateLogin(): Boolean {
        val value = getFieldValue(LOGIN_FIELD_ACTION_ID)
        return value.isNotEmpty()
    }

    private fun validatePassword(): Boolean {
        val value = getFieldValue(PASSWORD_FIELD_ACTION_ID)
        return value.isNotEmpty()
    }

    private fun validateCode(): Boolean {
        val value = getFieldValue(CODE_FIELD_ACTION_ID)
        return value.isEmpty() || value.isDigitsOnly()
    }

    private fun getFieldValue(actionId: Long): String {
        val action = findActionById(actionId)
        return action.editDescription?.toString().orEmpty()
    }

    private fun updateProgress(progress: Boolean) {
        (findButtonActionById(LOGIN_ACTION_ID) as? GuidedProgressAction)?.apply {
            showProgress = progress
            notifyButtonActionChanged(findButtonActionPositionById(id))
        }
    }

    private fun updateEnabled(actionId: Long, enabled: Boolean) {
        findButtonActionById(actionId)?.apply {
            isEnabled = enabled
            notifyButtonActionChanged(findButtonActionPositionById(actionId))
        }
        findActionById(actionId)?.apply {
            isEnabled = enabled
            notifyActionChanged(findActionPositionById(actionId))
        }
    }
}