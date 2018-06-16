package ru.radiationx.anilibria.model.data

class GoogleCaptchaException(
        val content: String,
        val url: String
) : Exception("Google Captcha")