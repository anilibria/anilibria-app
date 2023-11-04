package ru.radiationx.anilibria.ui.activities.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.security.KeyStoreException
import android.util.Log
import android.util.Rational
import android.view.WindowManager
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.session.MediaSession
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.net.CronetProviderInstaller
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import okhttp3.OkHttpClient
import org.chromium.net.CronetProvider
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ActivityVideoplayerBinding
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.media.mobile.models.PlayButtonState
import ru.radiationx.media.mobile.models.PlaylistItem
import ru.radiationx.media.mobile.models.TimelineSkip
import ru.radiationx.quill.get
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.getExtraNotNull
import java.io.IOException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.cert.CertPathValidatorException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate


private val Certificate.name: String
    get() = when (this) {
        is X509Certificate -> subjectDN.toString()
        else -> type
    }

fun PrintInstalledCertificates() {
    try {
        val ks = KeyStore.getInstance("AndroidCAStore")
        if (ks != null) {
            ks.load(null, null)
            val aliases = ks.aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement() as String
                val cert = ks.getCertificate(alias) as X509Certificate
                //To print System Certs only

                /* if (cert.issuerDN.name.contains("system")) {
                     println(cert.issuerDN.name)
                 }

                 //To print User Certs only
                 if (cert.issuerDN.name.contains("user")) {
                     println(cert.issuerDN.name)
                 }*/

                //To print all certs
                //println()
                Log.d("lololo", "keystore ${cert.issuerDN.name}")
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: KeyStoreException) {
        e.printStackTrace()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: CertificateException) {
        e.printStackTrace()
    }
}

private class PlayerHolder {
    private var _player: ExoPlayer? = null
    private var _mediaSession: MediaSession? = null

    fun init(context: Context) {
        CronetProviderInstaller.installProvider(context).addOnCompleteListener {
            Log.e("lololo", "cronet success")
        }
        CronetProvider.getAllProviders(context).forEach {
            Log.e("lololo", "cronet provider $it")
        }
        val okHttpClient = OkHttpClient.Builder()
            /*.addInterceptor { chain ->
                chain.proceed(chain.request()).also {
                    Log.e("lololo", "player certs:")
                    it.handshake?.peerCertificates?.forEach {
                        Log.e("lololo", "player cert ${it.name}")
                    }
                    Log.e("lololo", "player certs end")
                }
            }*/
            .build()
        okHttpClient.x509TrustManager?.acceptedIssuers?.forEach {
            //Log.e("lololo", "x509 ${it.issuerDN}")
        }
        val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient).apply {
        }

        /*val provider = CronetProvider.getAllProviders(context).first() {
            it.name == "App-Packaged-Cronet-Provider"
        }*/
        /*val engine = CronetEngine.Builder(context)
            .enableQuic(false)
            .enableHttp2(true)
            .enableBrotli(false)
            .addQuicHint("cache.libria.fun", 443, 443)
            .apply {
                repeat(30) {
                    addQuicHint("cache-cloud$it.libria.fun", 443, 443)
                }
            }


            .build()
        val engine1 = CronetUtil.buildCronetEngine(context)
        val cronetSourceFactory = engine?.let {
            CronetDataSource.Factory(it, Executors.newFixedThreadPool(4)).apply {
            }
        }*/


        val finalFactory = /*cronetSourceFactory ?:*/ okHttpDataSourceFactory
        val dataSourceFactory = DefaultDataSource.Factory(context, finalFactory)

        val mediaSourceFactory = DefaultMediaSourceFactory(context).apply {
            setDataSourceFactory(dataSourceFactory)
        }

        val player = ExoPlayer.Builder(context.applicationContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        val mediaSession = MediaSession.Builder(context, player).build()

        _mediaSession = mediaSession
        _player = player
    }

    fun destroy() {
        _mediaSession?.release()
        _mediaSession = null
        _player?.release()
        _player = null
    }

    fun getPlayer(): ExoPlayer {
        return requireNotNull(_player)
    }
}

class VideoPlayerActivity : BaseActivity(R.layout.activity_videoplayer) {

    companion object {
        private const val ARG_EPISODE_ID = "ARG_EPISODE_ID"
        private const val KEY_EPISODE_ID = "KEY_EPISODE_ID"
        private const val KEY_QUALITY = "KEY_QUALITY"
        private const val KEY_SPEED = "KEY_SPEED"

        fun newIntent(context: Context, episodeId: EpisodeId) =
            Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(ARG_EPISODE_ID, episodeId)
            }
    }


    private val playAction = PictureInPictureController.Action(
        code = 1,
        title = "Пуск",
        icRes = R.drawable.ic_media_play_arrow_24
    )

    private val pauseAction = PictureInPictureController.Action(
        code = 2,
        title = "Пауза",
        icRes = R.drawable.ic_media_pause_24
    )

    private val replayAction = PictureInPictureController.Action(
        code = 3,
        title = "Реплей",
        icRes = R.drawable.ic_media_replay_24
    )

    private val pipController by lazy { PictureInPictureController(this) }

    private val fullScreenController by lazy { FullScreenController(this) }

    private val settingDialogController by lazy {
        SettingDialogController(
            context = this,
            lifecycleOwner = this,
            appThemeController = get()
        )
    }

    private val binding by viewBinding<ActivityVideoplayerBinding>()

    private val player = PlayerHolder()

    private val viewModel by viewModel<PlayerViewModel> {
        PlayerExtra(getExtraNotNull(ARG_EPISODE_ID))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        initUiController()
        initFullscreenController()
        initPipController()
        initSettingsController()
        player.init(this)
        binding.playerView.setPlayer(player.getPlayer())
        //initAnalytics()

        handleEpisode(intent, savedInstanceState)

        viewModel.actions.onEach { action ->
            when (action) {
                is PlayerAction.InitialPlay -> {
                    val episodes = action.episodes.asReversed()
                    val items = episodes.map { episode ->
                        val mediaItem = MediaItem.Builder()
                            .setMediaId(episode.id.toString())
                            .setUri(Uri.parse(episode.url))
                            .setTag(episode)
                            .build()
                        val skips = listOfNotNull(
                            episode.skips?.opening,
                            episode.skips?.ending
                        ).map {
                            TimelineSkip(it.start, it.end)
                        }
                        PlaylistItem(mediaItem, skips)
                    }
                    val index = episodes.indexOfFirst { it.id == action.episodeId }
                    binding.playerView.prepare(items, index, action.seek)
                }

                is PlayerAction.PlaylistChange -> {
                    val episodes = action.episodes.asReversed()
                    val items = episodes.map { episode ->
                        val mediaItem = MediaItem.Builder()
                            .setMediaId(episode.id.toString())
                            .setUri(Uri.parse(episode.url))
                            .setTag(episode)
                            .build()
                        val skips = listOfNotNull(
                            episode.skips?.opening,
                            episode.skips?.ending
                        ).map {
                            TimelineSkip(it.start, it.end)
                        }
                        PlaylistItem(mediaItem, skips)
                    }
                    binding.playerView.changePlaylist(items)
                }

                is PlayerAction.Play -> {
                    if (action.seek != null && action.seek != 0L && action.seek != binding.playerView.timelineState.value.position) {
                        binding.playerView.seekTo(action.seek)
                    }
                    binding.playerView.play()
                }

                is PlayerAction.ShowSettings -> {
                    settingDialogController.showSettingsDialog(action.state)
                }
            }

        }.launchIn(lifecycleScope)

        viewModel.loadingState.onEach {
            binding.playerToolbarTitle.text = it.data?.title
            binding.playerToolbarSubtitle.text = it.data?.episodeTitle
        }.launchIn(lifecycleScope)

        viewModel.currentSpeed.onEach {
            binding.playerView.setSpeed(it)
        }.launchIn(lifecycleScope)


        binding.playerView.playerState.onEach {
            val episode = binding.playerView.playlistState.value.currentItem?.getEpisode()
            /*Log.e(
                "kekeke",
                "playerState ${it.playbackState}, ${it.isPlaying}, ${it.playWhenReady}, ${it.isLoading}, ${episode?.id}"
            )*/
        }.launchIn(lifecycleScope)

        binding.playerView.timelineState
            .sample(10000)
            .filter { it.duration > 0 }
            .map { it.position }
            .distinctUntilChanged()
            .onEach { position ->
                val episode = binding.playerView.playlistState.value.currentItem?.getEpisode()
                if (episode != null) {
                    viewModel.saveEpisodeSeek(episode.id, position)
                }
                //Log.d("kekeke", "timeline $position,  ${episode?.id}")
            }
            .launchIn(lifecycleScope)

        binding.playerView.mediaItemTransitionFlow
            .onEach { binding.playerView.pause() }
            .mapLatest { transition ->
                val timeline = binding.playerView.timelineState.first { it.duration > 0 }
                transition to timeline
            }
            .onEach { (transition, timeline) ->
                val episode = transition.mediaItem?.getEpisode() ?: return@onEach

                /*Log.e(
                    "kekeke",
                    "mediaItemTransitionFlow ${transition.reason}, $episode, ${timeline}"
                )*/
                viewModel.onEpisodeChanged(episode.id, timeline.duration)
            }
            .launchIn(lifecycleScope)
        binding.playerView.mediaItemTransitionFlow.onEach {

        }.launchIn(lifecycleScope)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleEpisode(intent, null)
    }

    override fun onStop() {
        super.onStop()
        binding.playerView.pause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_EPISODE_ID, viewModel.episodeId.value)
        outState.putSerializable(KEY_QUALITY, viewModel.targetQuality.value)
        outState.putFloat(KEY_SPEED, viewModel.currentSpeed.value)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.playerView.setPlayer(null)
        player.destroy()
    }

    private fun handleEpisode(intent: Intent, bundle: Bundle?) {
        val episodeId: EpisodeId = bundle?.getExtra(KEY_EPISODE_ID)
            ?: intent.getExtraNotNull(ARG_EPISODE_ID)

        val quality = bundle?.getExtra<PlayerQuality>(KEY_QUALITY)
        val speed = bundle?.getExtra<Float>(KEY_SPEED)
        viewModel.playEpisode(episodeId, quality, speed)
    }

    private fun PlaylistItem.getEpisode(): EpisodeState {
        return mediaItem.getEpisode()
    }

    private fun MediaItem.getEpisode(): EpisodeState {
        return localConfiguration?.tag as EpisodeState
    }

    private fun initAnalytics() {
        PrintInstalledCertificates()
        player.getPlayer()?.addAnalyticsListener(object : AnalyticsListener {
            val times = mutableListOf<Long>()
            val hostCounter = mutableMapOf<String, Int>()

            fun getCause(e: Throwable?): Throwable? {
                var cause: Throwable? = null
                var result = e
                while (null != result!!.cause.also { cause = it } && result !== cause) {
                    result = cause
                }
                return result
            }

            @UnstableApi
            override fun onLoadError(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData,
                error: IOException,
                wasCanceled: Boolean,
            ) {
                Log.e("lololo", "onLoadError $wasCanceled, ${loadEventInfo.uri}", error)
                (getCause(error) as? CertPathValidatorException?)?.also {

                    //Log.e("kekeke", "player error certPath")
                    //Log.e("kekeke", it.certPath.toString())
                    Log.e("lololo", "player error certs:")
                    it.certPath.certificates?.forEach {
                        Log.e("lololo", "player error cert ${it.name}")
                    }
                    Log.e("lololo", "player error certs end")
                }

                super.onLoadError(eventTime, loadEventInfo, mediaLoadData, error, wasCanceled)
            }

            @UnstableApi
            override fun onLoadCompleted(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData,
            ) {
                /*loadEventInfo.dataSpec.also {
                    Log.e(
                        "kekeke",
                        "onLoadCompleted kek ${it.httpMethodString}, ${it.uri}, ${it.customData}, ${it.flags}"
                    )
                    Log.e("kekeke", "onLoadCompleted headers ${it.httpRequestHeaders}")

                }*/
                times.add(loadEventInfo.loadDurationMs)
                val protocol = loadEventInfo.responseHeaders.let {
                    (it.get("x-server-proto") ?: it.get("X-Server-Proto"))?.firstOrNull()
                }.orEmpty()
                val frontHost = loadEventInfo.responseHeaders.let {
                    (it.get("front-hostname") ?: it.get("Front-Hostname"))?.firstOrNull()
                }.orEmpty()
                hostCounter[frontHost] = hostCounter.getOrPut(frontHost) { 0 } + 1
                Log.e(
                    "lololo",
                    "loadEventInfo.loadDurationMs ${loadEventInfo.loadDurationMs}, average ${
                        times.average().toLong()
                    }, protocol ${protocol}, hostname $frontHost[${hostCounter.get(frontHost)}]"
                )
                /*loadEventInfo.responseHeaders.also {
                    Log.e(
                        "kekeke",
                        "onLoadCompleted response headers ${it}"
                    )
                }*/
                super.onLoadCompleted(eventTime, loadEventInfo, mediaLoadData)
            }
        })
    }

    private fun initSettingsController() {
        settingDialogController.onQualitySelected = {
            viewModel.onQualitySelected(it)
        }

        settingDialogController.onSpeedSelected = {
            viewModel.onSpeedSelected(it)
        }
    }

    private fun initUiController() {
        binding.playerToolbarBack.setOnClickListener {
            finish()
        }
        val transition = AutoTransition().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            duration = 200
            addTarget(binding.playerToolbar)
        }
        binding.playerView.uiShowState.onEach {
            TransitionManager.beginDelayedTransition(binding.root, transition)
            binding.playerToolbar.isVisible = it
        }.launchIn(lifecycleScope)

        binding.playerView.onSettingsClick = {
            viewModel.onSettingsClick()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val barInsets = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val toolbarInsets = Insets.max(barInsets, cutoutInsets)
            binding.playerToolbar.updatePadding(
                left = toolbarInsets.left,
                top = toolbarInsets.top,
                right = toolbarInsets.right,
                bottom = toolbarInsets.bottom
            )
            binding.playerToolbarTitleContainer.updatePadding(
                left = toolbarInsets.right,
                right = toolbarInsets.left
            )
            insets
        }
    }

    private fun initFullscreenController() {
        WindowCompat.getInsetsController(window, binding.root).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.navigationBars())
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.displayCutout())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = if (isInMultiWindowMode) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        fullScreenController.init()
        fullScreenController.setFullscreen(true)
        fullScreenController.state.onEach {
            binding.playerView.setFullscreenVisible(it.available)
            binding.playerView.setFullscreenActive(it.actualFullScreen)
        }.launchIn(lifecycleScope)

        binding.playerView.onFullscreenClick = {
            fullScreenController.toggleFullscreen()
        }
    }

    private fun initPipController() {
        pipController.init()
        binding.playerView.outputState.onEach { videoOutputState ->
            val aspectRatio = videoOutputState.videoSize.let {
                Rational(it.width, it.height)
            }
            pipController.updateParams {
                it.copy(
                    sourceHintRect = videoOutputState.hintRect,
                    aspectRatio = aspectRatio
                )
            }
        }.launchIn(lifecycleScope)

        binding.playerView.playButtonState.onEach { playButtonState ->
            val actions = buildList {
                when (playButtonState) {
                    PlayButtonState.PLAY -> add(playAction)
                    PlayButtonState.PAUSE -> add(pauseAction)
                    PlayButtonState.REPLAY -> add(replayAction)
                }
            }
            pipController.updateParams {
                it.copy(actions = actions)
            }
        }.launchIn(lifecycleScope)

        pipController.state.onEach {
            binding.playerView.setPipVisible(it.canEnter)
            binding.playerView.setPipActive(it.active)
        }.launchIn(lifecycleScope)

        binding.playerView.onPipClick = {
            pipController.enter()
        }

        pipController.actionsListener = {
            when (it) {
                playAction, pauseAction, replayAction -> binding.playerView.handlePlayClick()
            }
        }
    }
}