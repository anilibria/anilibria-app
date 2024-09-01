package ru.radiationx.data.entity.domain.auth

class OtpNotFoundException(message: String) : RuntimeException(message)
class OtpAcceptedException(message: String) : RuntimeException(message)
class OtpNotAcceptedException(message: String) : RuntimeException(message)
