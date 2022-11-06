package ru.radiationx.anilibria.screen.main

import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import ru.radiationx.data.repository.YoutubeRepository
import ru.radiationx.shared_app.common.SystemUtils
import toothpick.InjectConstructor

@InjectConstructor
class MainYouTubeViewModel(
    private val youtubeRepository: YoutubeRepository,
    private val converter: CardsDataConverter,
    private val systemUtils: SystemUtils
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Обновления на YouTube"

    override val loadOnCreate: Boolean = false

    override fun onColdCreate() {
        super.onColdCreate()
        onRefreshClick()
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> = youtubeRepository
        .getYoutubeList(requestPage)
        .let { youtubeItems ->
            youtubeItems.data.map { converter.toCard(it) }
        }

    override fun onLibriaCardClick(card: LibriaCard) {
        val youtubeItem = card.rawData as YoutubeItem
        systemUtils.externalLink(youtubeItem.link)
    }
}