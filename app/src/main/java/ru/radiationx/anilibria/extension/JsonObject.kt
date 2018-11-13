package ru.radiationx.anilibria.extension

import org.json.JSONObject


fun JSONObject.nullString(field: String, fallback: String? = null): String? {
    if (isNull(field))
        return null
    return optString(field, fallback)
}

fun JSONObject.nullGet(field: String): Any? {
    if (isNull(field))
        return null
    return get(field)
}
