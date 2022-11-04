package ru.radiationx.shared.ktx.android

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

fun JSONArray.toObjectsList(): List<JSONObject> {
    val result = mutableListOf<JSONObject>()
    for (j in 0 until this.length()) {
        result.add(this.getJSONObject(j))
    }
    return result
}

fun <R> JSONArray.mapObjects(block: (JSONObject) -> R): List<R> {
    val result = mutableListOf<R>()
    for (j in 0 until this.length()) {
        val jsonObject = this.getJSONObject(j)
        result.add(block.invoke(jsonObject))
    }
    return result
}

fun <R> JSONArray.mapStrings(block: (String) -> R): List<R> {
    val result = mutableListOf<R>()
    for (j in 0 until this.length()) {
        val jsonObject = this.getString(j)
        result.add(block.invoke(jsonObject))
    }
    return result
}
