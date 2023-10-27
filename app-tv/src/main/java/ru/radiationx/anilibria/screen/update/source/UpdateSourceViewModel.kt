package ru.radiationx.anilibria.screen.update.source

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.update.UpdateController
import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.shared_app.common.SystemUtils
import toothpick.InjectConstructor

@InjectConstructor
class UpdateSourceViewModel(
    checkerRepository: CheckerRepository,
    private val guidedRouter: GuidedRouter,
    private val systemUtils: SystemUtils,
    private val updateController: UpdateController,
) : LifecycleViewModel() {

    val sourcesData = MutableStateFlow<List<UpdateData.UpdateLink>>(emptyList())

    init {
        checkerRepository
            .observeUpdate()
            .onEach {
                sourcesData.value = it.links
            }
            .launchIn(viewModelScope)
    }

    fun onLinkClick(index: Int) {
        viewModelScope.launch {
            guidedRouter.close()
            val link = sourcesData.value.getOrNull(index) ?: return@launch
            when (link.type) {
                UpdateData.LinkType.FILE -> updateController.downloadAction.emit(link)
                UpdateData.LinkType.SITE -> systemUtils.externalLink(link.url)
            }
        }
    }
}