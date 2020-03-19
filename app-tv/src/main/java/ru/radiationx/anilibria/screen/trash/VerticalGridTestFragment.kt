package ru.radiationx.anilibria.screen.trash

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridPresenter
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LoadingCard
import ru.radiationx.anilibria.common.MockData
import ru.radiationx.anilibria.common.fragment.BaseVerticalGridFragment
import ru.radiationx.anilibria.common.fragment.scoped.ScopedVerticalGridFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.screen.main.toCard
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.anilibria.ui.widget.CardDescriptionView
import javax.inject.Inject
import kotlin.math.max

class VerticalGridTestFragment : BaseVerticalGridFragment() {

    @Inject
    lateinit var mockData: MockData

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Избранное"
        gridPresenter = VerticalGridPresenter().apply {
            numberOfColumns = 5
        }

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
                    setDescription(item.errorTitle, item.errorDescription)
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
                addAll(0, mockData.releases.shuffled().map { it.toCard(requireContext()) })
                //add(LoadingCard())
                //add(LinkCard("Открыть избранное"))
            }
        }, (100..3000).random().toLong())

        this.adapter = adapter
    }
}