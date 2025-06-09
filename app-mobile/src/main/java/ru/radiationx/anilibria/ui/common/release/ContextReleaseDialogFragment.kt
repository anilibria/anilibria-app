package ru.radiationx.anilibria.ui.common.release

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra
import taiwa.TaiwaAction
import taiwa.TaiwaAnchor
import taiwa.common.DialogType
import taiwa.dialogs.NestedTaiwaDialogFragment

fun Fragment.showContextRelease(id: ReleaseId, release: Release) {
    ContextReleaseDialogFragment
        .newInstance(id, release)
        .show(childFragmentManager, "release_$id")
}

class ContextReleaseDialogFragment : NestedTaiwaDialogFragment(DialogType.BottomSheet) {

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_RELEASE = "release"

        fun newInstance(id: ReleaseId, release: Release): ContextReleaseDialogFragment {
            return ContextReleaseDialogFragment().putExtra {
                putParcelable(ARG_ID, id)
                putParcelable(ARG_RELEASE, release)
            }
        }
    }

    private val collectionAnchor = TaiwaAnchor.Id("collection")

    private val viewModel by viewModel<ReleaseItemViewModel> {
        ContextReleaseExtra(
            id = getExtraNotNull(ARG_ID),
            release = getExtraNotNull(ARG_RELEASE)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.onEach {
            bindState(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun bindState(state: ContextReleaseState) {
        nestedTaiwa.setContent {
            body {
                message {
                    text(state.title)
                }
                divider()
                if (state.hasAuth) {
                    item("favorite") {
                        if (state.isInFavorite) {
                            icon(R.drawable.ic_baseline_delete_outline_24)
                            title("Удалить из избранного")
                            tint(androidx.appcompat.R.attr.colorError)
                        } else {
                            icon(R.drawable.ic_fav_border)
                            title("Добавить в избранное")
                        }
                        action(TaiwaAction.Close)
                        onClick {
                            viewModel.onFavoriteClick()
                        }
                    }
                    item("collection") {
                        title("Коллекция")
                        icon(R.drawable.ic_collections)
                        value(state.collectionType?.toTitle() ?: "Не добавлено")
                        action(TaiwaAction.Anchor(collectionAnchor))
                        forward()
                    }
                }
                item("copy") {
                    icon(R.drawable.ic_baseline_content_copy_24)
                    title("Копировать ссылку")
                    action(TaiwaAction.Close)
                    onClick {
                        viewModel.onCopyClick()
                    }
                }
                item("share") {
                    icon(R.drawable.ic_baseline_share_24)
                    title("Поделиться")
                    action(TaiwaAction.Close)
                    onClick {
                        viewModel.onShareClick()
                    }
                }
                item("shortcut") {
                    icon(R.drawable.ic_baseline_app_shortcut_24)
                    title("Добавить на главный экран")
                    action(TaiwaAction.Close)
                    onClick {
                        viewModel.onShortcutClick()
                    }
                }
                if (state.isInHistory) {
                    divider()
                    item {
                        icon(R.drawable.ic_baseline_delete_outline_24)
                        tint(androidx.appcompat.R.attr.colorError)
                        title("Удалить из истории")
                        action(TaiwaAction.Close)
                        onClick { viewModel.onHistoryClick() }
                    }
                }
            }

            nested(collectionAnchor) {
                backAction(TaiwaAction.Root)
                header {
                    toolbar {
                        title("Коллекция")
                        withBack()
                    }
                }
                body {
                    state.collections.forEach { type ->
                        radioItem(type) {
                            title(type.toTitle())
                            val icRes = type.toIcRes()
                            if (icRes != null) {
                                icon(icRes)
                            } else {
                                emptyIcon()
                            }
                            select(type == state.collectionType)
                            action(TaiwaAction.Close)
                            onClick {
                                viewModel.onCollectionSelected(type)
                            }
                        }
                    }
                }
                if (state.collectionType != null) {
                    footer {
                        item("collection_delete") {
                            title("Удалить из коллекций")
                            action(TaiwaAction.Close)
                            icon(R.drawable.ic_baseline_delete_outline_24)
                            tint(androidx.appcompat.R.attr.colorError)
                            onClick {
                                viewModel.onCollectionSelected(null)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun CollectionType.toTitle(): String {
        return when (this) {
            CollectionType.Planned -> "Запланировано"
            CollectionType.Watching -> "Смотрю"
            CollectionType.Watched -> "Просмотрено"
            CollectionType.Postponed -> "Отложено"
            CollectionType.Abandoned -> "Брошено"
            is CollectionType.Unknown -> raw
        }
    }

    @DrawableRes
    private fun CollectionType.toIcRes(): Int? {
        return when (this) {
            CollectionType.Planned -> R.drawable.ic_collection_planned
            CollectionType.Watching -> R.drawable.ic_collection_watching
            CollectionType.Watched -> R.drawable.ic_collection_watched
            CollectionType.Postponed -> R.drawable.ic_collection_postponed
            CollectionType.Abandoned -> R.drawable.ic_collection_abandoned
            is CollectionType.Unknown -> null
        }
    }
}