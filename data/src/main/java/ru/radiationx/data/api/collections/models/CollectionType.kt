package ru.radiationx.data.api.collections.models

sealed interface CollectionType {
    companion object {
        val knownTypes = setOf(Planned, Watching, Watched, Postponed, Abandoned)
    }

    data object Planned : CollectionType
    data object Watching : CollectionType
    data object Watched : CollectionType
    data object Postponed : CollectionType
    data object Abandoned : CollectionType
    data class Unknown(val raw: String) : CollectionType
}