package ru.radiationx.data.api.collections.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface CollectionType : Parcelable {
    companion object {
        val knownTypes = setOf(Planned, Watching, Watched, Postponed, Abandoned)
    }

    @Parcelize
    data object Planned : CollectionType

    @Parcelize
    data object Watching : CollectionType

    @Parcelize
    data object Watched : CollectionType

    @Parcelize
    data object Postponed : CollectionType

    @Parcelize
    data object Abandoned : CollectionType

    @Parcelize
    data class Unknown(val raw: String) : CollectionType
}