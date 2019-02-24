package ru.radiationx.anilibria.model.system

import android.text.Html
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import javax.inject.Inject

@Suppress("DEPRECATION")
class ApiUtils  @Inject constructor(): IApiUtils {
    override fun toHtml(text: String?): CharSequence? {
        if (text == null)
            return null
        return Html.fromHtml(text)
    }

    override fun escapeHtml(text: String?): String? {
        if (text == null)
            return null
        return Html.fromHtml(text).toString()
    }
}
