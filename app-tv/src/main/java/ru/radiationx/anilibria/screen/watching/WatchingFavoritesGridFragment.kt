package ru.radiationx.anilibria.screen.watching

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.VerticalGridPresenter
import ru.radiationx.anilibria.common.CardDiffCallback
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.anilibria.common.LoadingCard
import ru.radiationx.anilibria.common.fragment.BaseVerticalGridFragment
import ru.radiationx.anilibria.screen.watching.WatchingFavoritesViewModel
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.quillParentViewModel
import kotlin.math.roundToInt

/**
 * "Избранное" в виде вертикального списка/сетки, с выплывающим снизу описанием
 * (использует BaseVerticalGridFragment для ShadowDescriptionView).
 * Также интегрируется в BrowseSupportFragment через MainFragmentAdapterProvider.
 */
class WatchingFavoritesGridFragment : BaseVerticalGridFragment(),
    BrowseSupportFragment.MainFragmentAdapterProvider {

    // Адаптер Leanback (горизонтальный?)
    // Но в нашем случае он применяется в вертикальном гриде
    private val adapter by lazy {
        ArrayObjectAdapter(
            CardPresenterSelector {
                // При встрече LinkCard
                viewModel.onLinkCardBind()
            }
        )
    }

    // Подключаем ViewModel для избранного
    private val viewModel by quillParentViewModel<WatchingFavoritesViewModel>()

    /**
     * MainFragmentAdapter нужен, чтобы BrowseSupportFragment мог управлять данным фрагментом
     * (анимации, переходы и т.д.).
     */
    private val mainFragmentAdapter =
        object : BrowseSupportFragment.MainFragmentAdapter<WatchingFavoritesGridFragment>(this) {

        }

    override fun getMainFragmentAdapter(): BrowseSupportFragment.MainFragmentAdapter<*> {
        return mainFragmentAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Настраиваем Presenter (кол-во колонок, zoom и т.д.)
        val gridPresenter = VerticalGridPresenter().apply {
            numberOfColumns = computeColumns()
        }
        setGridPresenter(gridPresenter)

        // Можно убрать/установить заголовок, если мешает
        // title = "Избранное (вертикально)"

        // Назначаем адаптер
        setAdapter(adapter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Важно, чтобы ViewModel получал onResume() / onPause() через LifecycleObserver
        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        // Подписываемся на список карточек
        subscribeTo(viewModel.cardsData) { cardList ->
            adapter.setItems(cardList, CardDiffCallback)
        }

        // Если хотите менять заголовок
        subscribeTo(viewModel.rowTitle) { newTitle ->
            // title = newTitle
        }

        // Клики по элементам
        setOnItemViewClickedListener(OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is LibriaCard -> viewModel.onLibriaCardClick(item)
                is LinkCard -> viewModel.onLinkCardClick()
                is LoadingCard -> viewModel.onLoadingCardClick()
            }
        })

        // При выборе (фокусе) карточки показываем снизу «тень» с текстом
        setOnItemViewSelectedListener(OnItemViewSelectedListener { _, item, _, _ ->
            if (item is LibriaCard) {
                // BaseVerticalGridFragment предоставляет эти методы:
                setDescription(item.title, item.description)
                setDescriptionVisible(true)
            } else {
                setDescription("", "")
                setDescriptionVisible(false)
            }
        })
    }



    private fun computeColumns(): Int {
        val displayMetrics = requireContext().resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val cardWidthDp = 180f
        val cardWidthPx = (cardWidthDp * displayMetrics.density).toInt()

        // Округляем к ближайшему целому
        val rawColumns = (screenWidth.toFloat() / cardWidthPx).roundToInt()
        // Не меньше 6
        return rawColumns.coerceAtLeast(6)
    }

}
