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

    private var currentGenres = ""

    override val loadOnCreate: Boolean = false

    override val defaultTitle: String = "Рекомендации"

    override fun onCreate() {
        super.onCreate()

        cardsData.value = listOf(loadingCard)

        releaseInteractor
            .observeFull(releaseId)
            .map { it.genres.take(3).joinToString() }
            .distinctUntilChanged()
            .lifeSubscribe {
                currentGenres = it
                onRefreshClick()
            }
    }

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = searchRepository
        .searchReleases(currentGenres, "", "", "2", "1", requestPage)
        .map { result ->
            result.data.filter { it.id != releaseId }.map { converter.toCard(it) }
        }

    override fun onLibriaCardClick(card: LibriaCard) {
        router.navigateTo(DetailsScreen(card.id))
    }
}