package ru.radiationx.data.entity.domain.auth

import ru.radiationx.data.datasource.remote.ApiError

open class ApiWrapperException(apiError: ApiError) : RuntimeException(apiError.message, apiError)

data class AlreadyAuthorizedException(val apiError: ApiError) : ApiWrapperException(apiError)
data class EmptyFieldException(val apiError: ApiError) : ApiWrapperException(apiError)
data class WrongUserAgentException(val apiError: ApiError) : ApiWrapperException(apiError)
data class InvalidUserException(val apiError: ApiError) : ApiWrapperException(apiError)
data class Wrong2FaCodeException(val apiError: ApiError) : ApiWrapperException(apiError)
data class WrongPasswordException(val apiError: ApiError) : ApiWrapperException(apiError)
