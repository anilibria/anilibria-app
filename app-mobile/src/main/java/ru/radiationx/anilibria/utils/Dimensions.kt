package ru.radiationx.anilibria.utils

import androidx.core.graphics.Insets

data class Dimensions(
    val statusBar: Int = 0,
    val navigationBar: Int = 0,
    val insets: Insets = Insets.NONE,
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0
)