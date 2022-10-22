package ru.radiationx.anilibria.extension

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.disableItemChangeAnimation() {
    (itemAnimator as? DefaultItemAnimator?)?.supportsChangeAnimations = false
}