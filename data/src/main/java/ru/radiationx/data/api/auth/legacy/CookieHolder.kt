package ru.radiationx.data.api.auth.legacy

import kotlinx.coroutines.flow.Flow
import okhttp3.Cookie

/**
 * Created by radiationx on 30.12.17.
 */
interface CookieHolder {
    suspend fun getAuthCookie(): Cookie?
    suspend fun removeAuthCookie()
}