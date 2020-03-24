package ru.radiationx.anilibria.screen.watching

import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.ReleaseRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class WatchingContinueViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val releaseRepository: ReleaseRepository,
    private val episodesCheckerHolder: EpisodesCheckerHolder,
    private val converter: CardsDataConverter,
    private val router: Router
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Продолжить просмотр"

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = episodesCheckerHolder
        .getEpisodes()
        .map {
            Log.e("lalala", "episoded ${it.map { it.lastAccess }}")
            it.sortedByDescending { it.lastAccess }.map { it.releaseId }
        }
        .flatMap {
            if (it.isEmpty()) {
                return@flatMap Single.just(emptyList<ReleaseFull>())
            }
            releaseRepository.getReleasesById(it)
        }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess {
            releaseInteractor.updateItemsCache(it)
        }
        .map { releases ->
            releases.map { converter.toCard(it) }
        }

    override fun hasMoreCards(newCards: List<LibriaCard>, allCards: List<LibriaCard>): Boolean = false

    override fun onLibriaCardClick(card: LibriaCard) {
        super.onLibriaCardClick(card)
        router.navigateTo(DetailsScreen(card.id))
    }
}