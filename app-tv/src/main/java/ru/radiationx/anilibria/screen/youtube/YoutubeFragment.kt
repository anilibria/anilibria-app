package ru.radiationx.anilibria.screen.youtube

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.VerticalGridPresenter
import ru.radiationx.anilibria.common.*
import ru.radiationx.anilibria.common.fragment.GridFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.subscribeTo

class YoutubeFragment : GridFragment() {

    private val gridAdapter by lazy {
        ArrayObjectAdapter(CardPresenterSelector {
            viewModel.onLinkCardBind()
        })
    }

    private val backgroundManager by lazy { GradientBackgroundManager(requireActivity()) }

    private val viewModel by viewModel<YouTubeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        gridPresenter = VerticalGridPresenter().apply {
            numberOfColumns = 2
        }
        onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, _ ->
            backgroundManager.applyCard(item)
            when (item) {
                is LibriaCard -> setDescription(item.title, item.description)
                is LinkCard -> setDescription(item.title, "")
                is LoadingCard -> setDescription(item.title, item.description)
                else -> setDescription("", "")
            }
        }

        this.adapter = gridAdapter

        subscribeTo(viewModel.cardsData) {
            gridAdapter.setItems(it, CardDiffCallback)
        }
    }
}
