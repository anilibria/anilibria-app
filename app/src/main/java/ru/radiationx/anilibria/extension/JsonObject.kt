package com.mintrocket.gisdelivery.extension

import org.json.JSONObject


fun JSONObject.nullString(field: String, fallback: String? = null): String? {
    if (isNull(field))
        return null
    return optString(field, fallback)
}
