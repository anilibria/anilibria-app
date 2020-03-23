package ru.radiationx.anilibria.screen.details

import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.data.entity.app.release.ReleaseItem
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
        .searchReleases(getGenres(genresCount), "", "", "2", "1", requestPage)
        .map { result -> result.data.filter { it.id != releaseId } }

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = searchGenres(3, requestPage)
        .flatMap {
            if (it.isEmpty()) {
                searchGenres(2, requestPage)
            } else {
                Single.just(it)
            }
        }
        .map { result ->
            result.map { converter.toCard(it) }
        }

    private fun getGenres(count: Int): String {
        val release = releaseInteractor.getFull(releaseId) ?: return ""
        return release.genres.take(count).joinToString()
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        router.navigateTo(DetailsScreen(card.id))
    }
}