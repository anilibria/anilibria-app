package ru.radiationx.anilibria.common

import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class LibriaCardRouter(
    private val router: Router,
    private val systemUtils: SystemUtils
) {

    fun navigate(libriaCard: LibriaCard) {
        when (libriaCard.type) {
            LibriaCard.Type.RELEASE -> {
                val release = requireNotNull(libriaCard.rawData as? Release?) {
                    "Release can't be null when type is release"
                }
                router.navigateTo(DetailsScreen(release.id))
            }
            LibriaCard.Type.YOUTUBE -> {
                val youtube = requireNotNull(libriaCard.rawData as? YoutubeItem?) {
                    "Youtube can't be null when type is youtube"
                }
                systemUtils.externalLink(youtube.link)
            }
        }
    }
}