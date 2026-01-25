package ru.radiationx.data.analytics.profile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import ru.radiationx.shared.ktx.coRunCatching

sealed interface ProfileAttribute {

    val name: kotlin.String

    data class String(
        override val name: kotlin.String,
        val value: kotlin.String
    ) : ProfileAttribute

    data class Number(
        override val name: kotlin.String,
        val value: kotlin.Number
    ) : ProfileAttribute

    data class Boolean(
        override val name: kotlin.String,
        val value: kotlin.Boolean
    ) : ProfileAttribute

    data class Error(
        override val name: kotlin.String,
        val value: Throwable
    ) : ProfileAttribute
}


internal fun String.mapToAttr(name: String) = ProfileAttribute.String(name, this)

internal fun Number.mapToAttr(name: String) = ProfileAttribute.Number(name, this)

internal fun Boolean.mapToAttr(name: String) = ProfileAttribute.Boolean(name, this)

internal fun CoroutineScope.asyncAttr(
    name: String,
    block: suspend (name: String) -> ProfileAttribute
): Deferred<ProfileAttribute> = async {
    coRunCatching {
        block.invoke(name)
    }.getOrElse {
        ProfileAttribute.Error(name, it)
    }
}