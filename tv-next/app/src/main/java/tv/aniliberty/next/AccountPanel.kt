package tv.aniliberty.next

import android.content.Context
import android.graphics.Bitmap
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The phone authorizes on the official website. TV only receives its own OTP session. */
class AccountPanel(
    context: Context,
    private val scope: CoroutineScope,
    private val account: AccountRepository,
    private val connected: () -> Unit,
) : TvSidePanel(context, "Вход в AniLiberty") {
    private var task: Job? = null
    private val link = "https://www.aniliberty.top/app/auth/otp/linkDevice"

    init {
        setOnDismissListener { task?.cancel() }
    }

    fun start() {
        addChoice("Войти по QR-коду") { requestCode() }
        addChoice("Войти по логину и паролю") { passwordForm() }
        addAction("Закрыть") { dismiss() }
        show()
    }

    fun startQr() {
        show()
        requestCode()
    }

    fun startPassword() {
        show()
        passwordForm()
    }

    private fun passwordForm() {
        task?.cancel()
        body.removeAllViews()
        footer.removeAllViews()
        heading.text = "Вход по логину и паролю"
        fun input(title: String, secret: Boolean): android.widget.EditText {
            body.addView(TextView(context).apply { label(title, 14f, MUTED) })
            return android.widget.EditText(context).apply {
                hint = title
                setTextColor(WHITE)
                setHintTextColor(MUTED)
                textSize = 16f
                isSingleLine = true
                inputType =
                    if (secret)
                        android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                    else
                        android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or
                            android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                imeOptions =
                    if (secret) android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                    else android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
                setPadding(dp(12), dp(8), dp(12), dp(8))
                webControlBackground()
                isSaveEnabled = false
                body.addView(
                    this,
                    LinearLayout.LayoutParams(-1, dp(48)).apply {
                        topMargin = dp(6)
                        bottomMargin = dp(16)
                    },
                )
            }
        }
        val login = input("Логин", false)
        val password = input("Пароль", true)
        val status = TextView(context).apply { label("", 14f, MUTED) }
        body.addView(status)
        var pendingToken: String? = null
        val submit =
            addAction("Войти", true) {
                if (task?.isActive == true) return@addAction
                if (login.text.isBlank() || password.text.isEmpty()) {
                    status.text = "Введите логин и пароль"
                    (if (login.text.isBlank()) login else password).requestFocus()
                    return@addAction
                }
                task =
                    scope.launch {
                        login.isEnabled = false
                        password.isEnabled = false
                        status.text = "Выполняем вход…"
                        try {
                            val token =
                                pendingToken
                                    ?: account.api
                                        .login(login.text.toString(), password.text.toString())
                                        .also { pendingToken = it }
                            account.connect(token)
                            password.text.clear()
                            dismiss()
                            connected()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: AccountHttpException) {
                            status.text =
                                if (e.status == 401 && pendingToken == null)
                                    "Неверный логин или пароль"
                                else if (pendingToken != null)
                                    "Вход подтверждён. Не удалось загрузить профиль. Повторите попытку."
                                else e.message
                        } catch (_: Exception) {
                            status.text = "Не удалось войти. Проверьте данные и подключение."
                        } finally {
                            login.isEnabled = true
                            password.isEnabled = true
                        }
                    }
            }
        val edited =
            object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    pendingToken = null
                }

                override fun afterTextChanged(s: android.text.Editable?) {}
            }
        login.addTextChangedListener(edited)
        password.addTextChangedListener(edited)
        password.setOnEditorActionListener { _, action, _ ->
            if (action == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                submit.performClick()
                true
            } else false
        }
        addAction("Войти по QR-коду") {
            password.text.clear()
            requestCode()
        }
        addAction("Отмена") {
            password.text.clear()
            dismiss()
        }
        login.requestFocus()
    }

    private fun requestCode() {
        heading.text = "Вход по QR-коду"
        task?.cancel()
        message("Получаем код устройства…")
        footer.removeAllViews()
        addAction("Отмена") { dismiss() }.requestFocus()
        task =
            scope.launch {
                var stage = "получить код устройства"
                try {
                    val device = account.session.deviceId
                    val code = account.api.deviceCode(device)
                    message("Отсканируйте QR-код и введите код на телефоне.")
                    body.addView(
                        TextView(context).apply {
                            label(code.code, 36f, WHITE, true)
                            gravity = Gravity.CENTER
                            letterSpacing = .12f
                            setPadding(0, dp(12), 0, dp(12))
                            contentDescription =
                                "Код устройства: ${code.code.toCharArray().joinToString(" ")}"
                        }
                    )
                    stage = "показать QR-код"
                    val matrix = QRCodeWriter().encode(link, BarcodeFormat.QR_CODE, 256, 256)
                    val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
                    val pixels =
                        IntArray(256 * 256) { i ->
                            if (matrix[i % 256, i / 256]) android.graphics.Color.BLACK
                            else android.graphics.Color.WHITE
                        }
                    bitmap.setPixels(pixels, 0, 256, 0, 0, 256, 256)
                    body.addView(
                        ImageView(context).apply {
                            setImageBitmap(bitmap)
                            contentDescription = "QR-код страницы привязки"
                        },
                        LinearLayout.LayoutParams(dp(156), dp(156)).apply {
                            gravity = Gravity.CENTER_HORIZONTAL
                        },
                    )
                    body.addView(
                        TextView(context).apply {
                            label("aniliberty.top/app/auth/otp/linkDevice", 12f, MUTED)
                            gravity = Gravity.CENTER
                            setPadding(0, dp(8), 0, dp(8))
                        }
                    )
                    val status =
                        TextView(context).apply {
                            label("Ожидаем подтверждение на телефоне…", 14f, MUTED)
                            gravity = Gravity.CENTER
                        }
                    body.addView(status)
                    val deadline =
                        android.os.SystemClock.elapsedRealtime() + code.remainingSeconds * 1000L
                    stage = "подтвердить вход"
                    val token = coroutineScope {
                        var progress = "Ожидаем подтверждение на телефоне…"
                        val ticker = launch {
                            while (true) {
                                val seconds =
                                    ((deadline - android.os.SystemClock.elapsedRealtime()) / 1000)
                                        .coerceAtLeast(0)
                                status.text = "$progress\nКод действует ещё $seconds с"
                                delay(1000)
                            }
                        }
                        try {
                            awaitDeviceLogin(deadline, status = { progress = it }) {
                                account.api.deviceLogin(device, code.code)
                            }
                        } finally {
                            ticker.cancel()
                        }
                    }
                    if (token != null) {
                        status.text = "Подтверждено. Загружаем профиль…"
                        suspend fun finishLogin() {
                            account.connect(token)
                            dismiss()
                            connected()
                        }
                        try {
                            finishLogin()
                            return@launch
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            status.text =
                                "Устройство подтверждено. Не удалось загрузить профиль. Можно повторить без нового кода."
                            footer.removeAllViews()
                            addAction("Завершить вход", true) {
                                task =
                                    scope.launch {
                                        try {
                                            finishLogin()
                                        } catch (e: CancellationException) {
                                            throw e
                                        } catch (_: Exception) {
                                            status.text =
                                                "Профиль пока недоступен. Проверьте соединение и повторите."
                                        }
                                    }
                            }
                            addAction("Закрыть") { dismiss() }
                            footer.getChildAt(0)?.requestFocus()
                            return@launch
                        }
                    }
                    message("Срок действия кода истёк. Получите новый код.")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val reason =
                        when (e) {
                            is java.net.UnknownHostException ->
                                "Не удалось найти сервер. Проверьте интернет и DNS на ТВ."
                            is java.net.SocketTimeoutException -> "Сервер не ответил вовремя."
                            is javax.net.ssl.SSLException ->
                                "Не удалось установить защищённое соединение. Проверьте дату и время на ТВ."
                            is AccountHttpException -> "Сервер ответил HTTP ${e.status}."
                            is AccountValidationException -> e.message ?: "Сервис отклонил запрос."
                            is org.json.JSONException -> "Сервис вернул неожиданный ответ."
                            is java.io.IOException -> "Нет соединения с сервером."
                            else -> "Ошибка ${e.javaClass.simpleName}."
                        }
                    message("Не удалось $stage.\n$reason")
                }
                footer.removeAllViews()
                addAction("Получить новый код", true) { requestCode() }
                addAction("Войти по паролю") { passwordForm() }
                addAction("Закрыть") { dismiss() }
                footer.getChildAt(0)?.requestFocus()
            }
    }
}
