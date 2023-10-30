package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Members(
    val timing: List<String>,
    val voicing: List<String>,
    val editing: List<String>,
    val decorating: List<String>,
    val translating: List<String>,
) : Parcelable