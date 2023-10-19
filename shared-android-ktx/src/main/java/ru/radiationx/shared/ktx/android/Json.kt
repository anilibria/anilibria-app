package ru.radiationx.shared.ktx.android

import org.json.JSONArray
import org.json.JSONObject


fun JSONObject.nullString(field: String): String? {
    if (isNull(field))
        return null
    return optString(field)
}

fun <R> JSONArray.mapObjects(block: (JSONObject) -> R): List<R> {
    val result = mutableListOf<R>()
    for (j in 0 until this.length()) {
        val jsonObject = this.getJSONObject(j)
        result.add(block.invoke(jsonObject))
    }
    return result
}

