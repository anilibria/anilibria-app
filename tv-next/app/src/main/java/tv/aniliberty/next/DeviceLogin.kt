package tv.aniliberty.next

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** A polling response is not a new code. Only the issued lifetime can expire this code. */
internal suspend fun awaitDeviceLogin(
    expiresAt: Long,
    now: () -> Long = { android.os.SystemClock.elapsedRealtime() },
    wait: suspend (Long) -> Unit = { delay(it) },
    status: (String) -> Unit = {},
    check: suspend () -> String,
): String? {
    while (now() < expiresAt) {
        wait(minOf(5000L, (expiresAt - now()).coerceAtLeast(0)))
        if (now() >= expiresAt) break
        try {
            return check()
        } catch (e: CancellationException) {
            throw e
        } catch (e: AccountHttpException) {
            when (e.status) {
                401,
                404 -> status("Ожидаем подтверждение на телефоне…")
                429 -> {
                    status("Сервис просит подождать. Код остаётся прежним.")
                    wait(minOf(10000L, (expiresAt - now()).coerceAtLeast(0)))
                }
                else ->
                    status("Проверка временно недоступна (${e.status}). Повторяем с тем же кодом…")
            }
        } catch (_: Exception) {
            status("Нет связи с сервисом. Повторяем проверку с тем же кодом…")
        }
    }
    return null
}
