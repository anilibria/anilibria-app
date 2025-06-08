package ru.radiationx.anilibria.ui.fragments.search.filter

import androidx.core.view.isVisible
import envoy.DiffItem
import envoy.ext.viewBindingEnvoy
import ru.radiationx.anilibria.databinding.ItemContentPlaceholderBinding

data class ContentPlaceholderState(
    val progress: Boolean,
    val error: Throwable?
) : DiffItem("placeholder")

fun contentPlaceholderEnvoy(
    refreshListener: () -> Unit
) = viewBindingEnvoy<ContentPlaceholderState, ItemContentPlaceholderBinding> {

    view.contentPlaceholder.setTitle("Ошибка")
    view.contentPlaceholder.setSecondaryButtonText("Обновить")

    view.contentPlaceholder.setSecondaryClickListener {
        refreshListener.invoke()
    }

    bind {
        view.contentProgress.isVisible = it.progress
        view.contentPlaceholder.isVisible = !it.progress && it.error != null
        view.contentPlaceholder.setSubtitle(it.error?.message)
    }
}