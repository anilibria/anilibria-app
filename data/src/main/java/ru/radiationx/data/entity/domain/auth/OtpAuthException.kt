package ru.radiationx.data.entity.domain.auth

class OtpNotCreatedException(message: String) : RuntimeException(message)
class OtpNotFoundException(message: String) : RuntimeException(message)
class OtpWrongUserException(message: String) : RuntimeException(message)
