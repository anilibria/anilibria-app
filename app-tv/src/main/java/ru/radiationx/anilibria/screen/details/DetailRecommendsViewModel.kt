package ru.radiationx.anilibria.screen.details

import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.search.SearchForm
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

    override val loadOnCreate: Boolean = false

    override val defaultTitle: String = "Рекомендации"

    override fun onCreate() {
        super.onCreate()

        cardsData.value = listOf(loadingCard)

        releaseInteractor
            .observeFull(releaseId)
            .distinctUntilChanged()
            .lifeSubscribe {
                onRefreshClick()
            }
    }

    private fun searchGenres(genresCount: Int, requestPage: Int): Single<List<ReleaseItem>> = searchRepository
        .searchReleases(SearchForm(genres = getGenres(genresCount), sort = SearchForm.Sort.RATING), requestPage)
        .map { result -> result.data.filter { it.id != releaseId } }

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = searchGenres(3, requestPage)
        .flatMap {
            if (it.isEmpty()) {
                searchGenres(2, requestPage)
            } else {
                Single.just(it)
            }
        }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess {
            releaseInteractor.updateItemsCache(it)
        }
        .map { result ->
            result.map { converter.toCard(it) }
        }

    private fun getGenres(count: Int): List<GenreItem> {
        val release = releaseInteractor.getFull(releaseId) ?: return emptyList()
        return release.genres.take(count).map { GenreItem(it, it) }
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        router.navigateTo(DetailsScreen(card.id))
    }
}