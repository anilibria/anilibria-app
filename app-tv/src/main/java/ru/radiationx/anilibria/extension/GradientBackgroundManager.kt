package ru.radiationx.anilibria.extension

import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.anilibria.common.LoadingCard

fun GradientBackgroundManager.applyCard(card: Any?) = when (card) {
    is LibriaCard -> applyImage(card.image)
    is LinkCard -> {
    }
    is LoadingCard -> {
    }
    null -> {
    }
    else -> clearGradient()
}