package ru.radiationx.anilibria.screen.main

import io.reactivex.Single
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.data.repository.YoutubeRepository
import toothpick.InjectConstructor

@InjectConstructor
class YouTubeViewModel(
    private val youtubeRepository: YoutubeRepository,
    private val converter: CardsDataConverter
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Обновления на YouTube"

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = youtubeRepository
        .getYoutubeList(requestPage)
        .map { youtubeItems ->
            youtubeItems.data.map { converter.toCard(it) }
        }
}