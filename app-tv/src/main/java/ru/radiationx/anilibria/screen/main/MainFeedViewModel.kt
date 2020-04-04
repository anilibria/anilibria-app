package ru.radiationx.anilibria.screen.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.FeedRepository
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor
import java.lang.RuntimeException

@InjectConstructor
class MainFeedViewModel(
    private val feedRepository: FeedRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val router: Router,
    private val systemUtils: SystemUtils
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Самое актуальное"

    override val loadOnCreate: Boolean = false

    override fun onColdCreate() {
        super.onColdCreate()
        onRefreshClick()
        Log.e("kekeke", "onColdCreate ${this::class.java.simpleName}")
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("kekeke", "onCreate ${this::class.java.simpleName}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("kekeke", "onDestroy ${this::class.java.simpleName}")
    }

    override fun onCleared() {
        super.onCleared()
        Log.e("kekeke", "onCleared ${this::class.java.simpleName}")
    }

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = feedRepository
        .getFeed(requestPage)
        .doOnSuccess {
            releaseInteractor.updateItemsCache(it.filter { it.release != null }.map { it.release!! })
        }
        .map { feedList -> feedList.map { converter.toCard(it) } }

    override fun onLibriaCardClick(card: LibriaCard) {
        super.onLibriaCardClick(card)
        when (card.type) {
            LibriaCard.Type.RELEASE -> {
                router.navigateTo(DetailsScreen(card.id))
            }
            LibriaCard.Type.YOUTUBE -> {
                val youtubeItem = card.rawData as YoutubeItem
                systemUtils.externalLink(youtubeItem.link)
            }
        }
    }
}