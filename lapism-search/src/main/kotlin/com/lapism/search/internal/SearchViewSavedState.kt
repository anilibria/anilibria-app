package com.lapism.search.internal

import android.os.Parcel
import android.os.Parcelable
import android.view.View

internal class SearchViewSavedState : View.BaseSavedState {
    var query: String? = null
    var hasFocus: Boolean = false

    internal constructor(superState: Parcelable?) : super(superState)

    internal constructor(`in`: Parcel) : super(`in`) {
        query = `in`.readString()
        hasFocus = `in`.readByte().toInt() != 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeString(query)
        out.writeByte((if (hasFocus) 1 else 0).toByte())
    }

    override fun toString(): String {
        return "SearchViewSavedState(query=$query, hasFocus=$hasFocus, hash=${hashCode()})"
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SearchViewSavedState> =
            object : Parcelable.Creator<SearchViewSavedState> {
                override fun createFromParcel(`in`: Parcel): SearchViewSavedState {
                    return SearchViewSavedState(`in`)
                }

                override fun newArray(size: Int): Array<SearchViewSavedState?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
