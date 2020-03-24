package ru.radiationx.anilibria.screen.watching

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.HistoryRepository
import ru.radiationx.data.repository.SearchRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor
import java.util.*

@InjectConstructor
class WatchingRecommendsViewModel(
    private val historyRepository: HistoryRepository,
    private val searchRepository: SearchRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val router: Router
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Рекомендации"

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = historyRepository
        .getReleases()
        .map { releases ->
            val genresMap = mutableMapOf<String, Int>()
            releases.forEach { release ->
                release.genres.forEach {
                    val currentCount = genresMap[it] ?: 0
                    genresMap[it] = currentCount + 1
                }
            }
            genresMap.toList().sortedByDescending { it.second }.take(3).joinToString()
        }
        .flatMap { genres ->
            searchRepository.searchReleases(genres, "", "", "2", "1", requestPage)
        }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess {
            releaseInteractor.updateItemsCache(it.data)
        }
        .map { result ->
            result.data.map { converter.toCard(it) }
        }

    override fun onLibriaCardClick(card: LibriaCard) {
        router.navigateTo(DetailsScreen(card.id))
    }

}