package ru.radiationx.anilibria.screen.details

import io.reactivex.Single
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.SearchRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DetailRecommendsViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val searchRepository: SearchRepository,
    private val converter: CardsDataConverter,
    private val router: Router
) : BaseCardsViewModel() {

    var releaseId: Int = -1

    override val defaultTitle: String = "Рекомендации"

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = searchRepository
        .searchReleases(getGenres(), "", "", "2", "1", requestPage)
        .map { result ->
            result.data.filter { it.id != releaseId }.map { converter.toCard(it) }
        }

    private fun getGenres(): String {
        val release = releaseInteractor.getFull(releaseId)
            ?: releaseInteractor.getItem(releaseId)
            ?: return ""
        return release.genres.take(2).joinToString()
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        router.navigateTo(DetailsScreen(card.id))
    }
}