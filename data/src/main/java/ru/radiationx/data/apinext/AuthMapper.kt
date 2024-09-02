package ru.radiationx.data.apinext

import anilibria.api.auth.models.LoginSocialResponse
import anilibria.api.auth.models.OtpGetResponse
import anilibria.api.auth.models.TokenResponse
import ru.radiationx.data.apinext.models.AuthToken
import ru.radiationx.data.apinext.models.LoginSocial
import ru.radiationx.data.apinext.models.SocialState
import ru.radiationx.data.entity.domain.auth.OtpInfo
import ru.radiationx.data.entity.mapper.secToMillis

fun OtpGetResponse.toDomain(): OtpInfo {
    return OtpInfo(
        code = otp.code,
        expiresAt = otp.expiredAt.apiDateToDate(),
        remainingTime = remainingTime.secToMillis()
    )
}

fun TokenResponse.toDomain(): AuthToken {
    return AuthToken(token)
}

fun LoginSocialResponse.toDomain(): LoginSocial {
    return LoginSocial(url = url, state = SocialState(state))
}