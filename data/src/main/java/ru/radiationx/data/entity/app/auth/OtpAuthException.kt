package ru.radiationx.data.entity.app.auth

import java.lang.RuntimeException

class OtpNotFoundException(message: String) : RuntimeException(message)
class OtpAcceptedException(message: String) : RuntimeException(message)
class OtpNotAcceptedException(message: String) : RuntimeException(message)
