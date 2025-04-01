// AuthOtpGuidedFragment.kt
package ru.radiationx.anilibria.screen.auth.otp

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import androidx.leanback.widget.GuidedActionsStylist
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
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
            .multilineDescription(true)
            .description("\nНажмите,\nкогда введёте код")
            .build()
    }

    private val expiredAction by lazy {
        GuidedProgressAction.Builder(requireContext())
            .id(EXPIRED_ACTION_ID)
            .title("Показать новый код")
            .description("\nВремя действия текущего кода\n истекло")
            .build()
    }

    private val repeatAction by lazy {
        GuidedProgressAction.Builder(requireContext())
            .id(REPEAT_ACTION_ID)
            .title("Повторить")
            .description("\nПроизошла ошибка")
            .build()
    }

    private val viewModel by viewModel<AuthOtpViewModel>()

    // Сохраняем ссылку на наш кастомный стилист,
    // чтобы потом обращаться к qrImageView, titleView, descriptionView и т.д.
    private lateinit var myGuidanceStylist: MyGuidanceStylist

    /**
     * Подключаем кастомный стилист вместо дефолтного.
     */
    override fun onCreateGuidanceStylist(): GuidanceStylist {
        // Возвращаем нашу кастомную реализацию
        myGuidanceStylist = MyGuidanceStylist()
        return myGuidanceStylist
    }

    /**
     * Задаём заголовок/подзаголовок/breadcrumb.
     * (Иконку можно тоже указать, если нужно.)
     */
    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        return GuidanceStylist.Guidance(
            "Запрашивается код",
            "Ожидаем код подтверждения...",
            "Авторизация",
            /* icon = */ null
        )
    }

    override fun onCreateActionsStylist(): GuidedActionsStylist {
        return GuidedProgressActionsStylist()
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        super.onCreateActions(actions, savedInstanceState)
        // При старте добавляем кнопку COMPLETE
        actions.add(completeAction)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Подключаем ViewModel
        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        // Подписка на данные (OTP-код и т.д.)
        subscribeTo(viewModel.otpInfoData.filterNotNull()) { otpInfo ->
            // Меняем title/description в кастомной шапке
            myGuidanceStylist.titleView?.text = "Код: ${otpInfo.code}"
            myGuidanceStylist.descriptionView?.text = otpInfo.description

            // Генерируем QR и ставим в qrImageView
            val qrBitmap = generateQrBitmap(otpInfo.code, 300)
            myGuidanceStylist.qrImageView?.setImageBitmap(qrBitmap)
        }

        // Подписка на состояние (кнопки, ошибки и т.д.)
        subscribeTo(viewModel.state) { state ->
            val primaryAction = when (state.buttonState) {
                AuthOtpViewModel.ButtonState.COMPLETE -> completeAction
                AuthOtpViewModel.ButtonState.EXPIRED -> expiredAction
                AuthOtpViewModel.ButtonState.REPEAT -> repeatAction
            }

            actions = if (state.error.isEmpty()) {
                listOf(primaryAction)
            } else {
                val errorAction = GuidedAction.Builder(requireContext())
                    .title("Ошибка")
                    .multilineDescription(true)
                    .description(state.error)
                    .infoOnly(true)
                    .focusable(false)
                    .build()
                listOf(primaryAction, errorAction)
            }

            primaryAction.updateProgress(state.progress)
        }
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
        // Чтобы корректно отобразилось на экране Leanback
        findButtonActionById(id)?.apply {
            block()
            notifyButtonActionChanged(findButtonActionPositionById(id))
        }
        findActionById(id)?.apply {
            block()
            notifyActionChanged(findActionPositionById(id))
        }
    }

    /**
     * Пример простой генерации QR через ZXing (3.5.1)
     */
    private fun generateQrBitmap(text: String, size: Int): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter()
            .encode(text, BarcodeFormat.QR_CODE, size, size)

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return bmp
    }
}
