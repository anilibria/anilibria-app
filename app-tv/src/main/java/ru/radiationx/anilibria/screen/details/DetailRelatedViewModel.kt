package ru.radiationx.anilibria.screen.details

import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.ReleaseRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class DetailRelatedViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val releaseRepository: ReleaseRepository,
    private val converter: CardsDataConverter,
    private val router: Router
) : BaseCardsViewModel() {

    var releaseId: Int = -1

    override val loadOnCreate: Boolean = false

    override val defaultTitle: String = "Связанные релизы"

    override fun onCreate() {
        super.onCreate()

        releaseInteractor
            .observeFull(releaseId)
            .distinctUntilChanged()
            .lifeSubscribe {
                onRefreshClick()
            }
    }

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> {
        val release = releaseInteractor.getFull(releaseId) ?: releaseInteractor.getItem(releaseId)
        val releaseCodes = DetailsViewModel.getReleasesFromDesc(release?.description.orEmpty())
        if (releaseCodes.isEmpty()) {
            return Single.just(emptyList())
        }
        val singles = releaseCodes.map { releaseRepository.getRelease(it) }
        return Single.zip(singles) {
                it.map { it as ReleaseFull }.toList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { releases ->
                Log.e("kekeke", "related releases ${releases.map { it.id }}")
                releases.map { converter.toCard(it) }
            }
    }

    override fun hasMoreCards(newCards: List<LibriaCard>, allCards: List<LibriaCard>): Boolean = false

    override fun onLibriaCardClick(card: LibriaCard) {
        router.navigateTo(DetailsScreen(card.id))
    }
}