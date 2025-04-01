package ru.radiationx.anilibria.screen.player

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.filterNotNull
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.quill.get
import ru.radiationx.quill.getViewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.subscribeTo

@OptIn(UnstableApi::class)
class PlayerFragment : BasePlayerFragment() {

    companion object {
        private const val ARG_RELEASE_ID = "release id"
        private const val ARG_EPISODE_ID = "episode id"

        fun newInstance(
            releaseId: ReleaseId,
            episodeId: ru.radiationx.data.entity.domain.types.EpisodeId?,
        ): PlayerFragment = PlayerFragment().putExtra {
            putParcelable(ARG_RELEASE_ID, releaseId)
            putParcelable(ARG_EPISODE_ID, episodeId)
        }
    }

    /**
     * ID «исходного» релиза (сезона), с которым открыли плеер.
     * Если в плеере переключились на другой сезон (releaseId),
     * и хотим при выходе вернуться к новому сезону — сравниваем с этим значением.
     */
    private val argumentsReleaseId by lazy { getExtraNotNull<ReleaseId>(ARG_RELEASE_ID) }

    // 1) Router нужно получить после onAttach().
    // Поэтому делаем lateinit- переменную, а инициализацию в onAttach()
    private lateinit var router: Router

    // 2) ViewModel тоже нельзя получать в момент объявления,
    //    лучше инициализировать в onCreate() (уже attach’d).
    private lateinit var viewModel: PlayerViewModel

    /**
     * Вызывается до onCreate(). Здесь фрагмент уже «прикреплён» к Activity,
     * так что можно безопасно вызывать get<Router>().
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        router = get() // import ru.radiationx.quill.get
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Здесь Fragment уже attach’d к Activity, и можно безопасно получить ViewModel
        viewModel = getViewModel(PlayerViewModel::class) {
            PlayerExtra(
                releaseId = argumentsReleaseId,
                episodeId = getExtra(ARG_EPISODE_ID)
            )
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        playerGlue?.actionListener = object : VideoPlayerGlue.OnActionClickedListener {
            override fun onPrevious() = viewModel.onPrevClick(getPosition())
            override fun onNext() = viewModel.onNextClick(getPosition())
            override fun onQualityClick() = viewModel.onQualityClick(getPosition())
            override fun onSpeedClick() = viewModel.onSpeedClick()
            override fun onEpisodesClick() = viewModel.onEpisodesClick(getPosition())
        }
        progressBarManager.initialDelay = 0
        progressBarManager.show()

        // Подписываемся на обновления данных для видео
        subscribeTo(viewModel.videoData.filterNotNull()) {
            progressBarManager.hide()
            playerGlue?.apply {
                title = it.title
                subtitle = it.subtitle
                seekTo(it.seek)
                preparePlayer(it.url)
            }
            skipsPart?.setSkips(it.skips)
        }

        // Подписываемся на сигнал Play/Pause
        subscribeTo(viewModel.playAction.filterNotNull()) { isPlay ->
            if (isPlay) {
                playerGlue?.play()
            } else {
                playerGlue?.pause()
            }
        }

        // Скорость воспроизведения
        subscribeTo(viewModel.speedState.filterNotNull()) { speedValue ->
            player?.playbackParameters =
                androidx.media3.common.PlaybackParameters(speedValue)
        }

        // Качество (меняем иконку в управлении)
        subscribeTo(viewModel.qualityState.filterNotNull()) {
            playerGlue?.setQuality(it)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseClick(getPosition())
    }

    /**
     * Событие окончания воспроизведения серии
     */
    override fun onCompletePlaying() {
        viewModel.onComplete(getPosition())
    }

    /**
     * Событие «плеер готов воспроизводить» (получили реальную длительность и т.д.)
     */
    override fun onPreparePlaying() {
        viewModel.onPrepare(getDuration())
    }

    /**
     * Если хотим при **выходе** из плеера (свайп назад или системная «назад») проверять,
     * не ушли ли мы на другой сезон (releaseId), — делаем это в onStop().
     *
     * Можно делать и в onDestroyView(), однако onStop() надёжнее с точки зрения
     * «экран уже точно закрываем».
     */
    override fun onStop() {
        super.onStop()
        // Узнаём, на каком releaseId (сезоне) мы закончили
        val newReleaseId = viewModel.getCurrentReleaseId() ?: return

        // Если он отличается от исходного (аргумента), значит был переход на другой сезон
        if (newReleaseId != argumentsReleaseId) {
            router.replaceScreen(DetailsScreen(newReleaseId))
        }
    }

    private fun getPosition(): Long = player?.currentPosition ?: 0

    private fun getDuration(): Long = player?.duration ?: 0
}
