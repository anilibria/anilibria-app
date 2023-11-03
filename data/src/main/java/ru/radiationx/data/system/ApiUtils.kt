package ru.radiationx.data.system

import androidx.core.text.parseAsHtml
import ru.radiationx.data.datasource.remote.IApiUtils
import javax.inject.Inject

class ApiUtils @Inject constructor() : IApiUtils {

    override fun toHtml(text: String?): CharSequence? {
        return text?.parseAsHtml()
    }

    override fun escapeHtml(text: String?): String? {
        return text?.parseAsHtml()?.toString()
    }
}
