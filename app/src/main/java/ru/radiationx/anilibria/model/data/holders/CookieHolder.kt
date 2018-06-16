package ru.radiationx.anilibria.model.data.holders

import okhttp3.Cookie

/**
 * Created by radiationx on 30.12.17.
 */
interface CookieHolder {
    companion object {
        const val BITRIX_SM_LOGIN = "BITRIX_SM_LOGIN"
        const val BITRIX_SM_SOUND_LOGIN_PLAYED = "BITRIX_SM_SOUND_LOGIN_PLAYED"
        const val BITRIX_SM_USER_AUTH = "BITRIX_SM_USER_AUTH"
        const val BITRIX_SM_UIDH = "BITRIX_SM_UIDH"
        const val BITRIX_SM_UIDL = "BITRIX_SM_UIDL"
        const val PHPSESSID = "PHPSESSID"
        const val BLAZINGFAST_RCKSID = "rcksid"
        const val BLAZINGFAST_WEB_PROTECT = "BLAZINGFAST-WEB-PROTECT"

        val cookieNames = listOf(
                BITRIX_SM_LOGIN,
                BITRIX_SM_SOUND_LOGIN_PLAYED,
                BITRIX_SM_USER_AUTH,
                BITRIX_SM_UIDH,
                BITRIX_SM_UIDL,
                PHPSESSID,
                BLAZINGFAST_RCKSID,
                BLAZINGFAST_WEB_PROTECT
        )
    }

    fun getCookies(): Map<String, Cookie>
    fun putCookie(url: String, cookie: Cookie)
    fun putCookie(url: String, name: String, value: String)
    fun removeCookie(name: String)
}