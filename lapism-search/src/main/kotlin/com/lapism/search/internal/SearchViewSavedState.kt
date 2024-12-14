package com.lapism.search.internal

import android.os.Parcelable
import android.view.View
import androidx.annotation.RestrictTo
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
@Parcelize
internal class SearchViewSavedState @JvmOverloads constructor(
    @IgnoredOnParcel
    val state: Parcelable? = null,
    var query: String?,
    var hasFocus: Boolean
) : View.BaseSavedState(state) {

    override fun toString(): String {
        return "SearchViewSavedState(query=$query, hasFocus=$hasFocus, hash=${hashCode()})"
    }
}
