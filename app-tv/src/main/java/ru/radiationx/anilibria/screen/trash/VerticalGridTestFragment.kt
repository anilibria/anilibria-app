package ru.radiationx.anilibria.screen.trash

import android.os.Bundle
import android.os.Handler
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridPresenter
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.LoadingCard
import ru.radiationx.anilibria.common.MockData
import ru.radiationx.anilibria.common.fragment.BaseVerticalGridFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.screen.main.toCard
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import javax.inject.Inject

class VerticalGridTestFragment : BaseVerticalGridFragment() {

    @Inject
    lateinit var mockData: MockData

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Избранное"

        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = 5
        setGridPresenter(gridPresenter)

        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            backgroundManager.applyCard(item)
        }

        prepareEntranceTransition()



        val presenterSelector = CardPresenterSelector()
        val adapter = ArrayObjectAdapter(presenterSelector).apply {
            //add(LoadingCard())
        }


        Handler().postDelayed({
            adapter.apply {
                startEntranceTransition()
                clear()
                addAll(0, mockData.releases.shuffled().map { it.toCard(requireContext()) })
                add(LoadingCard())
                //add(LinkCard("Открыть избранное"))
            }
        }, (100..3000).random().toLong())

        this.adapter = adapter
    }
}