package ru.radiationx.anilibria.screen.youtube

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.VerticalGridPresenter
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.common.fragment.GridFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModel
import javax.inject.Inject

class YoutubeFragment : GridFragment() {

    private val gridAdapter by lazy { ArrayObjectAdapter(CardPresenterSelector()) }

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    private val viewModel by viewModel<YouTubeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)

        gridPresenter = VerticalGridPresenter().apply {
            numberOfColumns = 2
        }

        backgroundManager.clearGradient()
        onItemViewSelectedListener = OnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
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


        this.adapter = gridAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeTo(viewModel.cardsData) {
            gridAdapter.setItems(it, CardDiffCallback)
        }
    }
}