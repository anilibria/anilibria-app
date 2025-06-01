package ru.radiationx.data.api.auth.models

class OtpNotCreatedException(message: String) : RuntimeException(message)
class OtpNotFoundException(message: String) : RuntimeException(message)
class OtpWrongUserException(message: String) : RuntimeException(message)
