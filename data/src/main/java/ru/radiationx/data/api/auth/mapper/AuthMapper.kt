package ru.radiationx.data.api.auth.mapper

import android.net.Uri
import anilibria.api.auth.models.LoginSocialResponse
import anilibria.api.auth.models.OtpGetResponse
import anilibria.api.auth.models.TokenResponse
import ru.radiationx.data.api.auth.models.AuthToken
import ru.radiationx.data.api.auth.models.LoginSocial
import ru.radiationx.data.api.auth.models.OtpCode
import ru.radiationx.data.api.auth.models.OtpInfo
import ru.radiationx.data.api.auth.models.SocialState
import ru.radiationx.data.api.shared.apiDateToDate
import ru.radiationx.data.api.shared.secToMillis

fun OtpGetResponse.toDomain(): OtpInfo {
    return OtpInfo(
        code = OtpCode(otp.code),
        expiresAt = otp.expiredAt.apiDateToDate(),
        remainingTime = remainingTime.secToMillis()
    )
}

fun TokenResponse.toDomain(): AuthToken {
    return AuthToken(token)
}

fun LoginSocialResponse.toDomain(): LoginSocial {
    val redirectUrl = Uri.parse(url).getQueryParameter("redirect_uri")
    requireNotNull(redirectUrl) {
        "Redirect not found"
    }
    return LoginSocial(
        url = url,
        state = SocialState(state),
        redirectUrl = redirectUrl
    )
}