package ru.radiationx.anilibria.model.system

import android.text.Html
import ru.radiationx.anilibria.model.data.remote.IApiUtils

class ApiUtils : IApiUtils {
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
