package ru.radiationx.anilibria.screen.suggestions

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.app.search.SuggestionItem
import ru.radiationx.data.repository.SearchRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
class SuggestionsResultViewModel(
    private val searchRepository: SearchRepository,
    private val router: Router,
    private val suggestionsController: SuggestionsController
) : LifecycleViewModel() {

    private var currentQuery = ""
    private var queryRelay = PublishRelay.create<String>()

    val progressState = MutableLiveData<Boolean>()
    val resultData = MutableLiveData<List<LibriaCard>>()

    override fun onColdCreate() {
        super.onColdCreate()

        queryRelay
            .debounce(350L, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                if (it.length < 3) {
                    showItems(emptyList(), it, false)
                }
            }
            .filter { it.length >= 3 }
            .doOnNext { progressState.value = true }
            .switchMapSingle { query ->
                searchRepository
                    .fastSearch(query)
                    .onErrorReturnItem(emptyList())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe({
                showItems(it, currentQuery, true)
            }, {
                it.printStackTrace()
            })
    }

    fun onQueryChange(query: String) {
        currentQuery = query
        queryRelay.accept(currentQuery)
    }

    fun onCardClick(item: LibriaCard) {
        router.navigateTo(DetailsScreen(item.id))
    }

    private fun showItems(items: List<SuggestionItem>, query: String, validQuery: Boolean) {
        val result = SuggestionsController.SearchResult(items, query, validQuery)
        suggestionsController.resultEvent.accept(result)
        progressState.value = false
        resultData.value = items.map {
            LibriaCard(
                it.id,
                it.names.getOrNull(0).orEmpty(),
                it.names.getOrNull(1).orEmpty(),
                it.poster.orEmpty(),
                LibriaCard.Type.RELEASE
            )
        }
    }
}