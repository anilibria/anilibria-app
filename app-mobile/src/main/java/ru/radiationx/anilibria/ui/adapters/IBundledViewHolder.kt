package ru.radiationx.anilibria.ui.adapters

import android.os.Parcelable

interface IBundledViewHolder {
    fun getStateId(): Int
    fun saveState(): Parcelable?
    fun restoreState(state: Parcelable?)
}