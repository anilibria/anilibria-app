package ru.radiationx.anilibria.screen.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.repository.FeedRepository
import toothpick.InjectConstructor
import java.lang.RuntimeException

@InjectConstructor
class FeedViewModel(
    private val feedRepository: FeedRepository,
    private val converter: CardsDataConverter
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Самое актуальное"

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = feedRepository
        .getFeed(requestPage)
        /*.doOnSuccess {
            if ((0..1).random() == 1) {
                throw RuntimeException("Prosto privet kek dela")
            }
        }*/
        .map { feedList -> feedList.map { converter.toCard(it) } }
}