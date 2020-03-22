package ru.radiationx.anilibria.screen.watching

import io.reactivex.Single
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.data.repository.SearchRepository
import toothpick.InjectConstructor

@InjectConstructor
class RecommendsViewModel(
    private val searchRepository: SearchRepository,
    private val converter: CardsDataConverter
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Рекомендации"

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = searchRepository
        .searchReleases("", "", "", "2", "1", requestPage)
        .map { result ->
            result.data.map { converter.toCard(it) }
        }

}