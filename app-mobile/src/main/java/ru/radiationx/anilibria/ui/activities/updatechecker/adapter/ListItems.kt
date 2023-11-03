package ru.radiationx.anilibria.ui.activities.updatechecker.adapter

import ru.radiationx.anilibria.ui.activities.updatechecker.UpdateLinkState
import ru.radiationx.anilibria.ui.adapters.ListItem

data class UpdateLinkListItem(val data: UpdateLinkState) : ListItem(data.link)
data class UpdateInfoListItem(val title: String, val items: List<String>) : ListItem(title)