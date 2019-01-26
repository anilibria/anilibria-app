package ru.radiationx.anilibria.extension

import org.json.JSONArray
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

fun <T, R, C : MutableCollection<in R>> JSONArray.mapTo(destination: C, transform: (T) -> R): C {
    (0 until this.length()).forEach {
        destination.add(transform.invoke(this.get(it) as T))
    }
    return destination
}
