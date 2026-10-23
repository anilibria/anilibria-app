package tv.aniliberty.next

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class PlayerActivity : ComponentActivity() {
    private lateinit var root: FrameLayout
    private lateinit var video: PlayerView
    private lateinit var buffering: android.widget.ProgressBar
    private var errorPanel: TvSidePanel? = null
    private lateinit var chrome: PlayerChrome
    private lateinit var skipPrompt: LinearLayout
    private lateinit var floatingSkip: TextView
    private lateinit var floatingWatch: TextView
    private val skipOffers = SkipOfferState()
    private lateinit var seekPreview: TextView
    private lateinit var skipButton: TextView
    private var lastPlaying: Boolean? = null
    private val options by lazy { getSharedPreferences("player-options", MODE_PRIVATE) }
    private lateinit var controls: LinearLayout
    private lateinit var title: TextView
    private lateinit var clock: TextView
    private lateinit var seek: SeekBar
    private lateinit var previousButton: TextView
    private lateinit var pause: TextView
    private lateinit var episodesButton: TextView
    private lateinit var qualityButton: TextView
    private lateinit var nextButton: TextView
    private var panel: ViewGroup? = null
    private val panelPlayback = PanelPlayback()
    private var panelChild = false
    private var panelCaller: View? = null
    private var player: ExoPlayer? = null
    private var release: Release? = null
    private var current: Episode? = null
    private var quality = 0
    private var stopped = false
    private var desiredPlay = true
    private var pendingSeek: Long? = null
    private var nextOffer: LinearLayout? = null
    private var progressTicks = 0
    private var nextRemaining = 10
    private var countdownRunning = false
    private val handler = Handler(Looper.getMainLooper())
    private val remoteRepeat = RemoteRepeat()
    private val remoteClick = RemoteClick()
    private var revealKey = -1
    private var lastControl: View? = null
    private val settings by lazy { TvSettings(this) }
    private val skippedRanges = mutableSetOf<String>()
    private val prefs by lazy { playbackPreferences(this) }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun time(ms: Long) = playbackTime(ms)

    private val hide = Runnable {
        if (panel == null && pendingSeek == null && player?.isPlaying == true) hideControls()
    }
    private val ticker =
        object : Runnable {
            override fun run() {
                val engine = player
                if (engine != null) {
                    val duration = engine.duration.coerceAtLeast(0)
                    val position = pendingSeek ?: engine.currentPosition
                    if (engine.isPlaying && pendingSeek == null && panel == null) {
                        val episode = current
                        listOf("opening" to episode?.opening, "ending" to episode?.ending)
                            .forEach { (key, range) ->
                                val identity = "${episode?.id}:$key"
                                if (
                                    settings.enabled("skip-$key", false) &&
                                        identity !in skippedRanges &&
                                        !skipOffers.dismissed(identity)
                                ) {
                                    skipTarget(range, position, duration)?.let { target ->
                                        skippedRanges.add(identity)
                                        engine.seekTo(target)
                                        Toast.makeText(
                                                this@PlayerActivity,
                                                if (key == "opening") "Опенинг пропущен"
                                                else "Эндинг пропущен",
                                                Toast.LENGTH_SHORT,
                                            )
                                            .show()
                                    }
                                }
                            }
                    }
                    updateChrome()
                    val reachedWatchedThreshold = current?.let { episode ->
                        isEpisodeWatched(engine.currentPosition, duration, settings.watchedPercent) &&
                            !isEpisodeWatched(
                                prefs.getLong("position:${episode.id}", 0),
                                duration,
                                settings.watchedPercent,
                            )
                    } == true
                    if (++progressTicks % 10 == 0 || reachedWatchedThreshold) save()
                }
                if (!stopped) handler.postDelayed(this, 1000)
            }
        }
    private val countdown =
        object : Runnable {
            override fun run() {
                if (!countdownRunning || stopped || panel != null) return
                nextRemaining--
                nextOffer?.getChildAt(0)?.let {
                    (it as TextView).text = "Следующая серия · $nextRemaining с"
                }
                if (nextRemaining <= 0) {
                    cancelNext()
                    nextEpisode(release?.episodes.orEmpty(), current?.id)?.let { startEpisode(it) }
                } else handler.postDelayed(this, 1000)
            }
        }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        quality = state?.getInt("quality") ?: 0
        desiredPlay = state?.getBoolean("playing") ?: true
        root = FrameLayout(this).apply { setBackgroundColor(android.graphics.Color.BLACK) }
        video = PlayerView(this).apply { useController = false }
        root.addView(video, FrameLayout.LayoutParams(-1, -1))
        buffering =
            android.widget.ProgressBar(this).apply {
                isFocusable = false
                contentDescription = "Загрузка видео"
            }
        root.addView(buffering, FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER))
        chrome = PlayerChrome(this)
        controls = chrome
        title = chrome.title
        clock = chrome.elapsed
        seek = chrome.seek
        root.addView(chrome, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))
        chrome.visibility = View.GONE
        seekPreview =
            TextView(this).apply {
                label("", 34f, WHITE, true)
                gravity = Gravity.CENTER
                setPadding(dp(24), dp(14), dp(24), dp(14))
                background = shape(0xDD181818.toInt(), dp(12).toFloat())
                visibility = View.GONE
                isFocusable = false
            }
        root.addView(seekPreview, FrameLayout.LayoutParams(-2, -2, Gravity.CENTER))
        skipPrompt =
            LinearLayout(this).apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(8), dp(8), dp(8))
                background = shape(0xE8181818.toInt(), dp(8).toFloat())
                visibility = View.GONE
            }
        floatingSkip =
            action(skipPrompt, "Пропустить опенинг", true, R.drawable.ic_next) { skipSegment() }
        floatingWatch = action(skipPrompt, "Смотреть опенинг") { watchSegment() }
        skipPrompt.addView(
            floatingSkip,
            LinearLayout.LayoutParams(-2, dp(48)).apply { marginEnd = dp(8) },
        )
        skipPrompt.addView(floatingWatch, LinearLayout.LayoutParams(-2, dp(48)))
        root.addView(
            skipPrompt,
            FrameLayout.LayoutParams(-2, -2, Gravity.END or Gravity.BOTTOM).apply {
                setMargins(dp(28), dp(28), dp(28), dp(28))
            },
        )
        seek.setOnKeyListener { _, key, event ->
            when (key) {
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        val engine = player
                        pendingSeek =
                            remoteSeekTarget(
                                pendingSeek ?: engine?.currentPosition ?: 0,
                                engine?.duration ?: 0,
                                key == KeyEvent.KEYCODE_DPAD_RIGHT,
                                event.eventTime - event.downTime,
                            )
                        refreshControls()
                    }
                    true
                }
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER -> {
                    if (event.action == KeyEvent.ACTION_UP) {
                        pendingSeek?.let { player?.seekTo(it) }
                        pendingSeek = null
                        refreshControls()
                    }
                    true
                }
                else -> false
            }
        }
        previousButton =
            chrome.icon("Предыдущая серия", R.drawable.ic_previous) {
                val items = release?.episodes.orEmpty()
                items.getOrNull(items.indexOfFirst { it.id == current?.id } - 1)?.let {
                    startEpisode(it)
                }
            }
        pause = chrome.icon("Пауза", R.drawable.ic_pause, true) { togglePause() }
        nextButton =
            chrome.icon("Следующая серия", R.drawable.ic_next) {
                nextEpisode(release?.episodes.orEmpty(), current?.id)?.let { startEpisode(it) }
            }
        chrome.buttons.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
        episodesButton = chrome.button("Серии", R.drawable.ic_episodes) { episodes() }
        qualityButton = chrome.button("Настройки", R.drawable.ic_settings) { playerSettings() }
        skipButton =
            action(chrome.skipActions, "Пропустить опенинг", true, R.drawable.ic_next) {
                skipSegment()
            }
        chrome.skipActions.addView(
            skipButton,
            LinearLayout.LayoutParams(-2, dp(48)).apply { marginEnd = dp(8) },
        )
        setContentView(root)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    remoteClick.cancel()
                    when {
                        panel != null -> if (panelChild) playerSettings() else closePanel()
                        pendingSeek != null -> {
                            pendingSeek = null
                            refreshControls()
                        }
                        nextOffer != null -> {
                            cancelNext()
                            showControls()
                        }
                        controls.visibility == View.VISIBLE -> hideControls()
                        else -> finish()
                    }
                }
            },
        )
        val id = intent.getIntExtra("release", 0)
        val episodeId = state?.getString("episode") ?: intent.getStringExtra("episode")
        lifecycleScope.launch {
            try {
                val item = Api().release(id)
                release = item
                if (item.blocked) error("Просмотр ограничен")
                val episode = item.episodes.find { it.id == episodeId } ?: error("Эпизод не найден")
                if (episode.streams.isEmpty()) error("Нет доступного потока")
                current = episode
                if (!stopped) startEpisode(episode, quality, desiredPlay)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (!isFinishing && !isDestroyed)
                    showPlaybackError("Просмотр недоступен или нет подключения.") { recreate() }
            }
        }
    }

    private fun showPlaybackError(message: String, retry: () -> Unit) {
        buffering.visibility = View.GONE
        errorPanel?.dismiss()
        val dialog = TvSidePanel(this, "Не удалось воспроизвести")
        errorPanel = dialog
        dialog.message(message)
        dialog.addChoice("Повторить", icon = R.drawable.ic_refresh) {
            dialog.dismiss()
            retry()
        }
        if (current?.streams?.size?.let { it > 1 } == true)
            dialog.addChoice("Выбрать качество", icon = R.drawable.ic_settings) {
                dialog.dismiss()
                qualities()
            }
        dialog.addAction("Вернуться к тайтлу") {
            dialog.dismiss()
            finish()
        }
        dialog.setOnCancelListener { if (current == null) finish() else showControls() }
        dialog.show()
    }

    private fun refreshControls() {
        updateChrome()
        handler.removeCallbacks(hide)
        handler.postDelayed(hide, 5000)
    }

    private fun manualSkipRange(): SkipSegment? {
        val engine = player ?: return null
        val episode = current ?: return null
        return listOf("opening" to episode.opening, "ending" to episode.ending)
            .firstNotNullOfOrNull { (key, range) ->
                val identity = "${episode.id}:$key"
                skipTarget(range, engine.currentPosition, engine.duration)?.let { stop ->
                    val name = if (key == "opening") "опенинг" else "эндинг"
                    SkipSegment(identity, "Пропустить $name", "Смотреть $name", stop)
                }
            }
    }

    private fun skipSegment() {
        val range = manualSkipRange() ?: return
        skippedRanges += range.key
        skipOffers.dismiss(range.key)
        player?.seekTo(range.stop)
        remoteClick.cancel()
        updateChrome()
        if (controls.visibility == View.VISIBLE) seek.requestFocus() else video.requestFocus()
        refreshControls()
    }

    private fun watchSegment() {
        val range = manualSkipRange() ?: return
        skipOffers.dismiss(range.key)
        skippedRanges += range.key
        remoteClick.cancel()
        updateChrome()
        if (controls.visibility == View.VISIBLE) seek.requestFocus() else video.requestFocus()
        refreshControls()
    }

    private fun updateChrome() {
        if (!::chrome.isInitialized) return
        val engine = player
        val duration = engine?.duration?.coerceAtLeast(0) ?: 0
        val position = pendingSeek ?: engine?.currentPosition ?: 0
        chrome.update(position, duration)
        current?.let { episode ->
            chrome.episode.text =
                listOf(
                        episode.label,
                        episode.streams.getOrNull(quality)?.first.orEmpty(),
                        "${options.getFloat("speed",1f)}×",
                    )
                    .filter { it.isNotBlank() }
                    .joinToString("  ·  ")
        }
        seek.secondaryProgress =
            if (duration > 0)
                (((engine?.bufferedPosition ?: 0).coerceIn(0, duration) * 1000) / duration).toInt()
            else 0
        seekPreview.visibility =
            if (pendingSeek != null && panel == null) View.VISIBLE else View.GONE
        if (pendingSeek != null) {
            val delta = position - (engine?.currentPosition ?: 0)
            seekPreview.text =
                "${time(position)}   ${if(delta>=0)"+"else"−"}${time(kotlin.math.abs(delta))}"
        }
        val playing = engine?.isPlaying == true
        if (::pause.isInitialized && playing != lastPlaying) {
            lastPlaying = playing
            pause.contentDescription = if (playing) "Пауза" else "Продолжить"
            pause.setCompoundDrawablesRelative(
                getDrawable(if (playing) R.drawable.ic_pause else R.drawable.ic_play)
                    ?.mutate()
                    ?.apply {
                        setBounds(0, 0, dp(24), dp(24))
                        setTintList(pause.textColors)
                    },
                null,
                null,
                null,
            )
        }
        if (::skipButton.isInitialized) {
            val range = manualSkipRange()
            val available =
                range != null && pendingSeek == null && panel == null && nextOffer == null
            if (!available && chrome.skipActions.hasFocus()) seek.requestFocus()
            chrome.skipActions.visibility = if (available) View.VISIBLE else View.GONE
            if (range != null) {
                skipButton.text = range.skipLabel
                floatingSkip.text = range.skipLabel
                floatingWatch.text = range.watchLabel
            }
            val visible =
                skipOffers.visible(range?.key, available && controls.visibility != View.VISIBLE)
            val appeared = visible && skipPrompt.visibility != View.VISIBLE
            if (!visible && skipPrompt.hasFocus()) {
                remoteClick.cancel()
                video.requestFocus()
            }
            skipPrompt.visibility = if (visible) View.VISIBLE else View.GONE
            if (appeared) floatingSkip.requestFocus()
        }
    }

    private fun showControls(focus: View? = null) {
        controls.visibility = View.VISIBLE
        skipPrompt.visibility = View.GONE
        (focus ?: pause).requestFocus()
        refreshControls()
    }

    private fun hideControls() {
        controls.visibility = View.GONE
        seekPreview.visibility = View.GONE
        skipPrompt.visibility = View.GONE
        pendingSeek = null
        video.requestFocus()
    }

    private fun togglePause() {
        val engine = player ?: return
        if (engine.isPlaying) engine.pause()
        else {
            if (engine.playbackState == Player.STATE_ENDED) engine.seekTo(0)
            engine.play()
        }
        desiredPlay = engine.playWhenReady
        save()
        if (panel == null && nextOffer == null) showControls() else updateChrome()
    }

    private fun startEpisode(
        episode: Episode,
        requestedQuality: Int = 0,
        play: Boolean = true,
        positionOverride: Long? = null,
    ) {
        val selected = requestedQuality.takeIf { it in episode.streams.indices } ?: 0
        val stream =
            episode.streams.getOrNull(selected)
                ?: run {
                    Toast.makeText(this, "Нет доступного потока", Toast.LENGTH_LONG).show()
                    return
                }
        errorPanel?.dismiss()
        errorPanel = null
        buffering.visibility = View.VISIBLE
        save()
        cancelNext()
        player?.release()
        current = episode
        quality = selected
        desiredPlay = play
        val engine = ExoPlayer.Builder(this).build()
        player = engine
        video.player = engine
        title.text = release?.title.orEmpty()
        chrome.episode.text =
            listOf(episode.label, stream.first, "${options.getFloat("speed",1f)}×")
                .joinToString("  ·  ")
        engine.setPlaybackSpeed(options.getFloat("speed", 1f))
        video.resizeMode =
            options.getInt("resize", androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT)
        lastPlaying = null
        previousButton.visibility =
            if (release?.episodes?.indexOfFirst { it.id == episode.id }?.let { it > 0 } == true)
                View.VISIBLE
            else View.GONE
        nextButton.visibility =
            if (nextEpisode(release?.episodes.orEmpty(), episode.id) != null) View.VISIBLE
            else View.GONE
        engine.addListener(
            object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    if (this@PlayerActivity.player === engine)
                        video.keepScreenOn =
                            engine.playWhenReady &&
                                engine.playbackState != Player.STATE_ENDED &&
                                engine.playerError == null
                }

                override fun onPlayerError(error: PlaybackException) {
                    if (player !== engine || stopped) return
                    save()
                    cancelNext()
                    showControls()
                    showPlaybackError("Повторите загрузку или выберите другое качество.") {
                        startEpisode(episode, quality, desiredPlay)
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (!isPlaying) save()
                }

                override fun onPlaybackStateChanged(state: Int) {
                    if (player !== engine) return
                    buffering.visibility =
                        if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                    if (state == Player.STATE_ENDED) {
                        save()
                        offerNext()
                    }
                }
            }
        )
        engine.setMediaItem(MediaItem.fromUri(stream.second))
        engine.prepare()
        val saved = prefs.getLong("position:${episode.id}", 0)
        val duration = prefs.getLong("duration:${episode.id}", 0)
        engine.seekTo(
            positionOverride ?: if (isEpisodeWatched(saved, duration, settings.watchedPercent)) 0 else saved
        )
        engine.playWhenReady = play
        if (panel == null) hideControls()
    }

    private fun save() {
        val engine = player ?: return
        val episode = current ?: return
        val item = release ?: return
        val duration = engine.duration.takeIf { it > 0 } ?: episode.duration * 1000
        if (
            engine.currentPosition == 0L &&
                engine.duration <= 0 &&
                prefs.getLong("position:${episode.id}", 0) > 0
        )
            return
        ViewingHistory(prefs).save(item, episode, engine.currentPosition, duration)
        queueAccountProgress(this, episode.id, engine.currentPosition, duration)
        prefs
            .edit()
            .putLong("position:${episode.id}", engine.currentPosition.coerceAtLeast(0))
            .putLong("duration:${episode.id}", duration)
            .putString("last:${item.id}", episode.id)
            .apply()
    }

    private fun pauseForPanel() {
        panelPlayback.open { player?.pause() }
        pendingSeek = null
        save()
        updateChrome()
    }

    private fun openPanel(caption: String, caller: View, build: (LinearLayout) -> View?) {
        pauseForPanel()
        remoteClick.cancel()
        panelChild = caption in listOf("Качество", "Скорость", "Масштаб изображения")
        cancelNext()
        handler.removeCallbacks(hide)
        panel?.let { root.removeView(it) }
        panelCaller = caller
        val content =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(20), dp(20), dp(20))
                setBackgroundColor(0xFF212121.toInt())
            }
        content.addView(
            TextView(this).apply { label(caption, 20f, WHITE, true) },
            LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(16) },
        )
        val target = build(content)
        content.addView(
            action(content, "Вернуться к видео", icon = R.drawable.ic_close) { closePanel() },
            LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(12) },
        )
        val scroll =
            ScrollView(this).apply {
                isFillViewport = true
                addView(content)
                isFocusable = false
            }
        panel = scroll
        controls.visibility = View.GONE
        seekPreview.visibility = View.GONE
        skipPrompt.visibility = View.GONE
        root.addView(
            scroll,
            FrameLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * .42f).toInt(),
                -1,
                Gravity.END,
            ),
        )
        scroll.post { (target ?: content.getChildAt(1)).requestFocus() }
    }

    private fun closePanel(resume: Boolean = true) {
        remoteClick.cancel()
        panel?.let { root.removeView(it) }
        panel = null
        panelChild = false
        panelPlayback.close(resume && !stopped) {
            desiredPlay = true
            player?.play()
        }
        showControls(panelCaller?.takeIf { it.isAttachedToWindow && it.isEnabled })
        panelCaller = null
    }

    private fun episodes() {
        val item = release ?: return
        pauseForPanel()
        save()
        remoteClick.cancel()
        panelChild = false
        cancelNext()
        handler.removeCallbacks(hide)
        panel?.let { root.removeView(it) }
        panelCaller = episodesButton
        val body =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(20), dp(16), dp(12))
                setBackgroundColor(0xF51A1A1A.toInt())
            }
        body.addView(
            TextView(this).apply { label("Серии · ${item.episodes.size}", 22f, WHITE, true) },
            LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) },
        )
        val list =
            RecyclerView(this).apply {
                tag = "player-episodes"
                layoutManager = LinearLayoutManager(this@PlayerActivity)
                itemAnimator = null
                adapter =
                    PlayerEpisodeAdapter(
                        item.episodes,
                        current?.id,
                        { id ->
                            prefs.getLong("position:$id", 0) to prefs.getLong("duration:$id", 0)
                        },
                    ) { episode ->
                        closePanel(false)
                        startEpisode(episode)
                    }
            }
        body.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        body.addView(
            action(body, "Вернуться к видео", icon = R.drawable.ic_close) { closePanel() },
            LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(12) },
        )
        panel = body
        controls.visibility = View.GONE
        seekPreview.visibility = View.GONE
        skipPrompt.visibility = View.GONE
        root.addView(
            body,
            FrameLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * .46f).toInt(),
                -1,
                Gravity.END,
            ),
        )
        val index = item.episodes.indexOfFirst { it.id == current?.id }.coerceAtLeast(0)
        list.doOnNextLayout {
            if (panel === body)
                list.findViewHolderForAdapterPosition(index)?.itemView?.requestFocus()
        }
        (list.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(index, dp(12))
        list.requestLayout()
    }

    private fun playerSettings() {
        openPanel("Настройки воспроизведения", qualityButton) { content ->
            fun choice(title: String, click: () -> Unit) {
                content.addView(
                    action(content, title, click = click).apply {
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    },
                    LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(8) },
                )
            }
            choice("Качество · ${current?.streams?.getOrNull(quality)?.first.orEmpty()}") {
                qualities()
            }
            choice("Скорость · ${options.getFloat("speed",1f)}×") {
                openPanel("Скорость", qualityButton) { values ->
                    var selected: View? = null
                    listOf(.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                        val button =
                            action(
                                values,
                                "${if(speed==options.getFloat("speed",1f))"✓  "else""}${speed}×",
                            ) {
                                options.edit().putFloat("speed", speed).apply()
                                player?.setPlaybackSpeed(speed)
                                closePanel()
                            }
                        values.addView(
                            button,
                            LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(8) },
                        )
                        if (speed == options.getFloat("speed", 1f)) selected = button
                    }
                    selected
                }
            }
            choice("Масштаб изображения") {
                openPanel("Масштаб изображения", qualityButton) { values ->
                    listOf(
                            "По размеру экрана" to
                                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT,
                            "Заполнить с обрезкой" to
                                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                        )
                        .forEach { (label, mode) ->
                            values.addView(
                                action(values, label) {
                                    options.edit().putInt("resize", mode).apply()
                                    video.resizeMode = mode
                                    closePanel()
                                },
                                LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(8) },
                            )
                        }
                    null
                }
            }
            choice("Считать просмотренной после · ${settings.watchedPercent}%") {
                showWatchedPercent(this) {
                    save()
                    playerSettings()
                }
            }
            listOf(
                    "auto-next" to "Следующая серия автоматически",
                    "skip-opening" to "Автопропуск опенинга",
                    "skip-ending" to "Автопропуск эндинга",
                )
                .forEach { (key, label) ->
                    val default =
                        if (key == "auto-next") prefs.getBoolean("auto-next", true) else false
                    choice("$label · ${if(settings.enabled(key,default))"Вкл."else"Выкл."}") {
                        settings.toggle(key, default)
                        playerSettings()
                    }
                }
            null
        }
    }

    private fun qualities() {
        val episode = current ?: return
        openPanel("Качество", qualityButton) { content ->
            var target: View? = null
            episode.streams.forEachIndexed { index, stream ->
                val button =
                    action(content, (if (index == quality) "✓  " else "") + stream.first) {
                        val playing = player?.playWhenReady ?: desiredPlay
                        startEpisode(episode, index, playing, player?.currentPosition)
                        closePanel()
                    }
                content.addView(
                    button,
                    LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(8) },
                )
                if (index == quality) target = button
            }
            target
        }
    }

    private fun cancelNext() {
        countdownRunning = false
        handler.removeCallbacks(countdown)
        nextOffer?.let { root.removeView(it) }
        nextOffer = null
    }

    private fun offerNext() {
        cancelNext()
        val episode =
            nextEpisode(release?.episodes.orEmpty(), current?.id)
                ?: run {
                    showControls()
                    return
                }
        if (panel != null || stopped) return
        if (episode.streams.isEmpty()) {
            showControls()
            Toast.makeText(this, "Следующая серия пока недоступна", Toast.LENGTH_LONG).show()
            return
        }
        val offer =
            LinearLayout(this).apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(8), dp(8), dp(8))
                background = shape(0xE8181818.toInt(), dp(8).toFloat())
            }
        nextOffer = offer
        nextRemaining = 10
        val auto = settings.enabled("auto-next", prefs.getBoolean("auto-next", true))
        val now =
            action(
                offer,
                if (auto) "Следующая серия · $nextRemaining с" else "Следующая серия",
                true,
                R.drawable.ic_next,
            ) {
                cancelNext()
                startEpisode(episode)
            }
        offer.addView(
            now,
            LinearLayout.LayoutParams(-2, dp(48)).apply { marginEnd = dp(8) },
        )
        offer.addView(
            action(offer, "Отмена") {
                cancelNext()
                showControls()
            },
            LinearLayout.LayoutParams(-2, dp(48)),
        )
        root.addView(
            offer,
            FrameLayout.LayoutParams(-2, -2, Gravity.END or Gravity.BOTTOM).apply {
                setMargins(dp(28), dp(28), dp(28), dp(28))
            },
        )
        hideControls()
        now.requestFocus()
        countdownRunning = auto
        if (auto) handler.postDelayed(countdown, 1000)
    }

    @android.annotation.SuppressLint(
        "RestrictedApi",
        "GestureBackNavigation",
    ) // Back is delegated to the registered OnBackPressedCallback.
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (remoteRepeat.discard(event)) return true
        if (event.action == KeyEvent.ACTION_UP && event.keyCode == revealKey) {
            revealKey = -1
            return true
        }
        val focused =
            panel?.findFocus()
                ?: nextOffer?.findFocus()
                ?: skipPrompt.findFocus()
                ?: controls.findFocus()
        if (
            focused !== seek &&
                (controls.visibility == View.VISIBLE ||
                    panel != null ||
                    nextOffer != null ||
                    skipPrompt.hasFocus()) &&
                remoteClick.handle(event, focused)
        )
            return true

        if (event.keyCode == KeyEvent.KEYCODE_BACK) return super.dispatchKeyEvent(event)
        if (
            skipPrompt.visibility == View.VISIBLE &&
                controls.visibility != View.VISIBLE &&
                panel == null &&
                nextOffer == null &&
                event.keyCode in
                    listOf(
                        KeyEvent.KEYCODE_DPAD_LEFT,
                        KeyEvent.KEYCODE_DPAD_RIGHT,
                        KeyEvent.KEYCODE_DPAD_UP,
                        KeyEvent.KEYCODE_DPAD_DOWN,
                    )
        ) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> floatingSkip.requestFocus()
                    KeyEvent.KEYCODE_DPAD_RIGHT -> floatingWatch.requestFocus()
                    else -> showControls(seek)
                }
            }
            return true
        }
        if (panel != null) {
            val direction =
                when (event.keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> View.FOCUS_LEFT
                    KeyEvent.KEYCODE_DPAD_RIGHT -> View.FOCUS_RIGHT
                    KeyEvent.KEYCODE_DPAD_UP -> View.FOCUS_UP
                    KeyEvent.KEYCODE_DPAD_DOWN -> View.FOCUS_DOWN
                    else -> 0
                }
            if (direction != 0) {
                if (event.action != KeyEvent.ACTION_DOWN) return true
                val list = panel?.findViewWithTag<RecyclerView>("player-episodes")
                val item = focused?.let { list?.findContainingItemView(it) }
                if (
                    list != null &&
                        item != null &&
                        direction in listOf(View.FOCUS_UP, View.FOCUS_DOWN)
                ) {
                    val index = list.getChildAdapterPosition(item)
                    val step = if (direction == View.FOCUS_DOWN) 1 else -1
                    val candidates =
                        if (step > 0) (index + 1 until (list.adapter?.itemCount ?: 0))
                        else (index - 1 downTo 0)
                    val target =
                        candidates.firstOrNull {
                            release?.episodes?.getOrNull(it)?.streams?.isNotEmpty() == true
                        }
                    if (target != null) {
                        val view = list.findViewHolderForAdapterPosition(target)?.itemView
                        if (view != null) view.requestFocus()
                        else {
                            list.doOnNextLayout {
                                if (panel != null)
                                    list
                                        .findViewHolderForAdapterPosition(target)
                                        ?.itemView
                                        ?.requestFocus()
                            }
                            list.scrollToPosition(target)
                            list.requestLayout()
                        }
                    } else if (step > 0)
                        panel?.getFocusables(View.FOCUS_FORWARD)?.lastOrNull()?.requestFocus()
                    return true
                }
                val candidate = focused?.focusSearch(direction)
                if (candidate != null && insidePanel(candidate)) candidate.requestFocus()
                return true
            }
            return super.dispatchKeyEvent(event)
        }
        if (nextOffer != null) {
            val direction =
                when (event.keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> View.FOCUS_LEFT
                    KeyEvent.KEYCODE_DPAD_RIGHT -> View.FOCUS_RIGHT
                    KeyEvent.KEYCODE_DPAD_UP -> View.FOCUS_UP
                    KeyEvent.KEYCODE_DPAD_DOWN -> View.FOCUS_DOWN
                    else -> 0
                }
            if (direction != 0) {
                val target = currentFocus?.focusSearch(direction)
                if (target == null || !insideView(target, nextOffer!!)) return true
            }
            return super.dispatchKeyEvent(event)
        }
        when (event.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) togglePause()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0)
                    nextEpisode(release?.episodes.orEmpty(), current?.id)?.let { startEpisode(it) }
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    val items = release?.episodes.orEmpty()
                    items.getOrNull(items.indexOfFirst { it.id == current?.id } - 1)?.let {
                        startEpisode(it)
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                if (event.action == KeyEvent.ACTION_DOWN) player?.play()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    player?.pause()
                    save()
                }
                return true
            }
            KeyEvent.KEYCODE_MENU -> {
                if (event.action == KeyEvent.ACTION_DOWN) episodes()
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (controls.visibility != View.VISIBLE) {
                    if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                        revealKey = event.keyCode
                        if (
                            event.keyCode in
                                listOf(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER)
                        )
                            togglePause()
                        else if (
                            event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN &&
                                skipPrompt.visibility == View.VISIBLE
                        )
                            floatingSkip.requestFocus()
                        else
                            showControls(
                                if (event.keyCode == KeyEvent.KEYCODE_DPAD_UP) seek else null
                            )
                    }
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (controls.visibility != View.VISIBLE) {
                    if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                        showControls(seek)
                        pendingSeek =
                            remoteSeekTarget(
                                player?.currentPosition ?: 0,
                                player?.duration ?: 0,
                                event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT,
                                0,
                            )
                    }
                    return true
                }
            }
        }
        if (controls.visibility == View.VISIBLE && panel == null && nextOffer == null) {
            val direction = event.keyCode
            if (focused === seek && direction == KeyEvent.KEYCODE_DPAD_UP) {
                if (
                    event.action == KeyEvent.ACTION_DOWN &&
                        chrome.skipActions.visibility == View.VISIBLE
                )
                    skipButton.requestFocus()
                return true
            }
            if (focused === skipButton) {
                if (
                    direction in
                        listOf(
                            KeyEvent.KEYCODE_DPAD_UP,
                            KeyEvent.KEYCODE_DPAD_DOWN,
                            KeyEvent.KEYCODE_DPAD_LEFT,
                            KeyEvent.KEYCODE_DPAD_RIGHT,
                        )
                ) {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        when (direction) {
                            KeyEvent.KEYCODE_DPAD_DOWN -> seek.requestFocus()
                            KeyEvent.KEYCODE_DPAD_LEFT -> skipButton.requestFocus()
                            KeyEvent.KEYCODE_DPAD_RIGHT -> skipButton.requestFocus()
                        }
                        refreshControls()
                    }
                    return true
                }
            }
            if (focused === seek && direction == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (event.action == KeyEvent.ACTION_DOWN)
                    (lastControl?.takeIf { it.isShown && it.isEnabled } ?: pause).requestFocus()
                return true
            }
            if (
                focused !== seek &&
                    focused != null &&
                    insideView(focused, controls) &&
                    direction in
                        listOf(
                            KeyEvent.KEYCODE_DPAD_UP,
                            KeyEvent.KEYCODE_DPAD_DOWN,
                            KeyEvent.KEYCODE_DPAD_LEFT,
                            KeyEvent.KEYCODE_DPAD_RIGHT,
                        )
            ) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    lastControl = focused
                    if (direction == KeyEvent.KEYCODE_DPAD_UP) seek.requestFocus()
                    else if (direction != KeyEvent.KEYCODE_DPAD_DOWN) {
                        val choices =
                            chrome.buttons.getFocusables(View.FOCUS_FORWARD).filter {
                                it.isEnabled && it.isShown
                            }
                        val index = choices.indexOf(focused)
                        choices
                            .getOrNull(
                                index + if (direction == KeyEvent.KEYCODE_DPAD_RIGHT) 1 else -1
                            )
                            ?.requestFocus()
                    }
                    refreshControls()
                }
                return true
            }
        }
        if (event.action == KeyEvent.ACTION_DOWN) refreshControls()
        return super.dispatchKeyEvent(event)
    }

    private fun insidePanel(view: View): Boolean = panel?.let { insideView(view, it) } ?: false

    private fun insideView(view: View, container: View): Boolean {
        var node: android.view.ViewParent? = view.parent
        while (node != null) {
            if (node === container) return true
            node = node.parent
        }
        return false
    }

    override fun onStart() {
        super.onStart()
        stopped = false
        handler.post(ticker)
        if (player == null) current?.let { startEpisode(it, quality, desiredPlay) }
    }

    override fun onStop() {
        video.keepScreenOn = false
        remoteClick.cancel()
        errorPanel?.dismiss()
        errorPanel = null
        stopped = true
        desiredPlay = player?.playWhenReady ?: desiredPlay
        save()
        cancelNext()
        handler.removeCallbacksAndMessages(null)
        video.player = null
        player?.release()
        player = null
        super.onStop()
    }

    override fun onSaveInstanceState(out: Bundle) {
        out.putString("episode", current?.id)
        out.putInt("quality", quality)
        out.putBoolean("playing", player?.playWhenReady ?: desiredPlay)
        super.onSaveInstanceState(out)
    }
}
