package ru.radiationx.anilibria.screen.player.episodes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerController
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.shared.ktx.asTimeSecString
import toothpick.InjectConstructor
import java.util.*

@InjectConstructor
class PlayerEpisodesViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter,
    private val playerController: PlayerController
) : LifecycleViewModel() {

    lateinit var argReleaseId: ReleaseId
    var argEpisodeId: EpisodeId? = null

    val episodesData = MutableLiveData<List<Pair<String, String?>>>()
    val selectedIndex = MutableLiveData<Int>()

    private val currentEpisodes = mutableListOf<Episode>()

    override fun onCreate() {
        super.onCreate()

        releaseInteractor.getFull(argReleaseId)?.also {
            currentEpisodes.clear()
            currentEpisodes.addAll(it.episodes.reversed())
        }
        episodesData.value = currentEpisodes.map {
            val description = if (it.access.isViewed && it.access.seek > 0) {
                "Остановлена на ${Date(it.access.seek).asTimeSecString()}"
            } else {
                null
            }
            Pair(it.title.orEmpty(), description)
        }
        selectedIndex.value = currentEpisodes.indexOfLast { it.id == argEpisodeId }
    }

    fun applyEpisode(index: Int) {
        viewModelScope.launch {
            playerController.selectEpisodeRelay.emit(currentEpisodes[index].id)
            guidedRouter.close()
        }
    }
}