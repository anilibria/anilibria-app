package ru.radiationx.anilibria.screen.player.episodes

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.player.PlayerController
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.app.release.ReleaseFull
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

    var argReleaseId = -1
    var argEpisodeId = -1

    val episodesData = MutableLiveData<List<Pair<String, String?>>>()
    val selectedIndex = MutableLiveData<Int>()

    private val currentEpisodes = mutableListOf<ReleaseFull.Episode>()

    override fun onCreate() {
        super.onCreate()

        releaseInteractor.getFull(argReleaseId)?.also {
            currentEpisodes.clear()
            currentEpisodes.addAll(it.episodes.reversed())
        }
        episodesData.value = currentEpisodes.map {
            val description = if (it.isViewed && it.seek > 0) {
                "Остановлена на ${Date(it.seek).asTimeSecString()}"
            } else {
                null
            }
            Pair(it.title.orEmpty(), description)
        }
        selectedIndex.value = currentEpisodes.indexOfLast { it.id == argEpisodeId }
    }

    fun applyEpisode(index: Int) {
        playerController.selectEpisodeRelay.accept(currentEpisodes[index].id)
        guidedRouter.close()
    }
}