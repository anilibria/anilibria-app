package ru.radiationx.data.apinext.models.enums

sealed interface CollectionType {
    companion object {
        val knownTypes = setOf(Planned, Watched, Watching, Postponed, Abandoned)
    }

    data object Planned : CollectionType
    data object Watched : CollectionType
    data object Watching : CollectionType
    data object Postponed : CollectionType
    data object Abandoned : CollectionType
    data class Unknown(val raw: String) : CollectionType
}