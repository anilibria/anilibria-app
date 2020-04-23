package ru.radiationx.anilibria.screen.trash

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridPresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.common.fragment.BaseVerticalGridFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import javax.inject.Inject

class VerticalGridTestFragment : BaseVerticalGridFragment() {

    @Inject
    lateinit var mockData: MockData

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    @Inject
    lateinit var dataConverter: CardsDataConverter

    override fun onInflateTitleView(inflater: LayoutInflater, parent: ViewGroup, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.lb_search_titleview, parent, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Избранное"
        gridPresenter = VerticalGridPresenter().apply {
            numberOfColumns = 6
        }

        setOnSearchClickedListener {

        }

        backgroundManager.clearGradient()
        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            backgroundManager.applyCard(item)
            when (item) {
                is LibriaCard -> {
                    setDescription(item.title, item.description)
                }
                is LinkCard -> {
                    setDescription(item.title, "")
                }
                is LoadingCard -> {
                    setDescription(item.title, item.description)
                }
                else -> {
                    setDescription("", "")
                }
            }
        }

        prepareEntranceTransition()

        val presenterSelector = CardPresenterSelector()
        val adapter = ArrayObjectAdapter(presenterSelector)
        Handler().postDelayed({
            adapter.apply {
                startEntranceTransition()
                clear()
                addAll(0, mockData.releases.shuffled().map { dataConverter.toCard(it) })
                //add(LoadingCard())
                //add(LinkCard("Открыть избранное"))
            }
        }, (100..3000).random().toLong())

        this.adapter = adapter
    }
}