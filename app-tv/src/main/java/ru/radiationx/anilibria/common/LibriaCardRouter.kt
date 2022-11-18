package ru.radiationx.anilibria.common

import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class LibriaCardRouter(
    private val router: Router,
    private val systemUtils: SystemUtils
) {

    fun navigate(libriaCard: LibriaCard) {
        when (val type = libriaCard.type) {
            is LibriaCard.Type.Release -> {
                router.navigateTo(DetailsScreen(type.releaseId))
            }
            is LibriaCard.Type.Youtube -> {
                systemUtils.externalLink(type.link)
            }
        }
    }
}