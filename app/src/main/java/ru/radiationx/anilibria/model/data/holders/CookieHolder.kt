package ru.radiationx.anilibria.model.data.holders

import okhttp3.Cookie

/**
 * Created by radiationx on 30.12.17.
 */
interface CookieHolder {
    companion object {
        private const val BITRIX_SM_LOGIN = "BITRIX_SM_LOGIN"
        private const val BITRIX_SM_SOUND_LOGIN_PLAYED = "BITRIX_SM_SOUND_LOGIN_PLAYED"
        private const val BITRIX_SM_USER_AUTH = "BITRIX_SM_USER_AUTH"
        const val BITRIX_SM_UIDH = "BITRIX_SM_UIDH"
        const val BITRIX_SM_UIDL = "BITRIX_SM_UIDL"
        private const val PHPSESSID = "PHPSESSID"
        
        val cookieNames = listOf(
                BITRIX_SM_LOGIN,
                BITRIX_SM_SOUND_LOGIN_PLAYED,
                BITRIX_SM_USER_AUTH,
                BITRIX_SM_UIDH,
                BITRIX_SM_UIDL,
                PHPSESSID
        )
    }
    
    fun getCookies(): Map<String, Cookie>
    fun putCookie(url: String, cookie: Cookie)
    fun removeCookie(name: String)
}