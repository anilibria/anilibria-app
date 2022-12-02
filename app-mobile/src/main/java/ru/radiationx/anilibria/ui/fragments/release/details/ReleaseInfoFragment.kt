package ru.radiationx.anilibria.ui.fragments.release.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.DialogFileDownloadBinding
import ru.radiationx.anilibria.databinding.FragmentListBinding
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.presentation.release.details.ReleaseDetailScreenState
import ru.radiationx.anilibria.presentation.release.details.ReleaseEpisodeItemState
import ru.radiationx.anilibria.presentation.release.details.ReleaseInfoViewModel
import ru.radiationx.anilibria.ui.activities.MyPlayerActivity
import ru.radiationx.anilibria.ui.activities.WebPlayerActivity
import ru.radiationx.anilibria.ui.activities.toPrefQuality
import ru.radiationx.anilibria.ui.adapters.release.detail.EpisodeControlPlace
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseEpisodeControlDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseEpisodeDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseHeadDelegate
import ru.radiationx.anilibria.ui.fragments.ScopeFragment
import ru.radiationx.data.analytics.features.mapper.toAnalyticsPlayer
import ru.radiationx.data.analytics.features.mapper.toAnalyticsQuality
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.SourceEpisode
import ru.radiationx.data.entity.domain.release.TorrentItem
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.imageloader.showImageUrl
import java.net.URLConnection

class ReleaseInfoFragment : ScopeFragment(R.layout.fragment_list) {

    companion object {
        const val ARG_ID: String = "release_id"
        const val ARG_ID_CODE: String = "release_id_code"
    }

    private val releaseInfoAdapter: ReleaseInfoAdapter by lazy {
        ReleaseInfoAdapter(
            headListener = headListener,
            episodeListener = episodeListener,
            episodeControlListener = episodeControlListener,
            donationListener = { viewModel.onClickDonate() },
            donationCloseListener = {},
            torrentClickListener = viewModel::onTorrentClick,
            commentsClickListener = viewModel::onCommentsClick,
            episodesTabListener = viewModel::onEpisodeTabClick,
            remindCloseListener = viewModel::onRemindCloseClick,
            torrentInfoListener = { showTorrentInfoDialog() }
        )
    }

    private val systemUtils by inject<SystemUtils>()

    private val viewModel by viewModel<ReleaseInfoViewModel>()

    private val binding by viewBinding<FragmentListBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.also { bundle ->
            viewModel.releaseId = bundle.getParcelable(ARG_ID)
            viewModel.releaseIdCode = bundle.getParcelable(ARG_ID_CODE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = releaseInfoAdapter
            setHasFixedSize(true)
            disableItemChangeAnimation()
        }

        viewModel.state.onEach {
            showState(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.loadTorrentAction.observe().onEach {
            loadTorrent(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.playEpisodesAction.observe().onEach {
            playEpisodes(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.playContinueAction.observe().onEach {
            playContinue(it.release, it.startWith)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.playWebAction.observe().onEach {
            playWeb(it.link, it.code)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.playEpisodeAction.observe().onEach {
            playEpisode(it.release, it.episode, it.playFlag, it.quality)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.loadEpisodeAction.observe().onEach {
            downloadEpisode(it.episode, it.quality)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.showUnauthAction.observe().onEach {
            showFavoriteDialog()
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.showDownloadAction.observe().onEach {
            showDownloadDialog(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.showFileDonateAction.observe().onEach {
            showFileDonateDialog(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.showEpisodesMenuAction.observe().onEach {
            showEpisodesMenuDialog()
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.showContextEpisodeAction.observe().onEach {
            showLongPressEpisodeDialog(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(ARG_ID, viewModel.releaseId)
        outState.putParcelable(ARG_ID_CODE, viewModel.releaseIdCode)
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    private fun showState(state: ReleaseDetailScreenState) {
        state.data?.let { releaseInfoAdapter.bindState(it, state) }
    }

    private fun loadTorrent(torrent: TorrentItem) {
        torrent.url?.also { systemUtils.externalLink(it) }
    }

    private fun playEpisodes(release: Release) {
        playEpisode(release, release.episodes.last(), null, null)
    }

    private fun playContinue(release: Release, startWith: Episode) {
        playEpisode(release, startWith, MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE, null)
    }

    private fun <T> getUrlByQuality(qualityInfo: QualityInfo<T>, quality: Int): String {
        return when (quality) {
            MyPlayerActivity.VAL_QUALITY_SD -> qualityInfo.urlSd
            MyPlayerActivity.VAL_QUALITY_HD -> qualityInfo.urlHd
            MyPlayerActivity.VAL_QUALITY_FULL_HD -> qualityInfo.urlFullHd
            else -> qualityInfo.urlSd
        }.orEmpty()
    }

    private fun showDownloadDialog(url: String) {
        val context = context ?: return
        val titles = arrayOf("Внешний загрузчик", "Системный загрузчик")
        AlertDialog.Builder(context)
            .setItems(titles) { _, which ->
                viewModel.submitDownloadEpisodeUrlAnalytics()
                when (which) {
                    0 -> systemUtils.externalLink(url)
                    1 -> viewModel.downloadFile(url)
                }
            }
            .show()
    }

    private fun showFileDonateDialog(url: String) {
        val dialogBinding = DialogFileDownloadBinding.inflate(
            LayoutInflater.from(requireView().context),
            null,
            false
        )

        dialogBinding.root.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        dialogBinding.dialogFileImage.showImageUrl("file:///android_asset/libria_tyan_type3.png")

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .show()

        dialogBinding.dialogFilePatreonBtn.setOnClickListener {
            viewModel.onDialogPatreonClick()
            dialog.dismiss()
        }
        dialogBinding.dialogFileDonateBtn.setOnClickListener {
            viewModel.onDialogDonateClick()
            dialog.dismiss()
        }
        dialogBinding.dialogFileDownloadBtn.setOnClickListener {
            showDownloadDialog(url)
            dialog.dismiss()
        }
    }

    private fun showEpisodesMenuDialog() {
        val context = context ?: return
        val items = arrayOf(
            "Сбросить историю просмотров",
            "Отметить все как просмотренные"
        )
        AlertDialog.Builder(context)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> viewModel.onResetEpisodesHistoryClick()
                    1 -> viewModel.onCheckAllEpisodesHistoryClick()
                }
            }
            .show()
    }

    private fun showLongPressEpisodeDialog(episode: Episode) {
        val context = context ?: return
        val items = arrayOf(
            "Отметить как непросмотренная"
        )
        AlertDialog.Builder(context)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> viewModel.markEpisodeUnviewed(episode)
                }
            }
            .show()
    }

    private fun downloadEpisode(episode: SourceEpisode, quality: Int?) {
        val qualityInfo = QualityInfo(episode, episode.urlSd, episode.urlHd, episode.urlFullHd)
        if (quality == null) {
            selectQuality(qualityInfo, { selected ->
                viewModel.onDownloadLinkSelected(getUrlByQuality(qualityInfo, selected))
            }, true)
        } else {
            viewModel.onDownloadLinkSelected(getUrlByQuality(qualityInfo, quality))
        }
    }

    private fun playEpisode(
        release: Release,
        episode: Episode,
        playFlag: Int?,
        quality: Int?
    ) {
        val qualityInfo = QualityInfo(episode, episode.urlSd, episode.urlHd, episode.urlFullHd)
        selectPlayer({ playerType ->
            if (quality == null) {
                when (playerType) {
                    PreferencesHolder.PLAYER_TYPE_EXTERNAL -> {
                        selectQuality(qualityInfo, { selected ->
                            playExternal(release, episode, selected)
                        }, true)
                    }
                    PreferencesHolder.PLAYER_TYPE_INTERNAL -> {
                        selectQuality(qualityInfo, { selected ->
                            playInternal(release, episode, selected, playFlag)
                        })
                    }
                }
            } else {
                when (playerType) {
                    PreferencesHolder.PLAYER_TYPE_EXTERNAL -> playExternal(
                        release,
                        episode,
                        quality
                    )
                    PreferencesHolder.PLAYER_TYPE_INTERNAL -> playInternal(
                        release,
                        episode,
                        quality,
                        playFlag
                    )
                }
            }
        })
    }

    private fun selectPlayer(onSelect: (playerType: Int) -> Unit, forceDialog: Boolean = false) {
        if (forceDialog) {
            showSelectPlayerDialog(onSelect)
        } else {
            val savedPlayerType = viewModel.getPlayerType()
            when (savedPlayerType) {
                PreferencesHolder.PLAYER_TYPE_NO -> {
                    showSelectPlayerDialog(onSelect)
                }
                PreferencesHolder.PLAYER_TYPE_ALWAYS -> {
                    showSelectPlayerDialog(onSelect, false)
                }
                PreferencesHolder.PLAYER_TYPE_INTERNAL -> {
                    onSelect(PreferencesHolder.PLAYER_TYPE_INTERNAL)
                }
                PreferencesHolder.PLAYER_TYPE_EXTERNAL -> {
                    onSelect(PreferencesHolder.PLAYER_TYPE_EXTERNAL)
                }
            }
        }
    }

    private fun showSelectPlayerDialog(
        onSelect: (playerType: Int) -> Unit,
        savePlayerType: Boolean = true
    ) {
        val titles = arrayOf("Внешний плеер", "Внутренний плеер")
        val context = context ?: return
        AlertDialog.Builder(context)
            .setItems(titles) { dialog, which ->
                val playerType = when (which) {
                    0 -> PreferencesHolder.PLAYER_TYPE_EXTERNAL
                    1 -> PreferencesHolder.PLAYER_TYPE_INTERNAL
                    else -> -1
                }
                if (playerType != -1) {
                    if (savePlayerType) {
                        viewModel.setPlayerType(playerType)
                    }
                    onSelect.invoke(playerType)
                }
            }
            .show()
    }

    private fun playInternal(
        release: Release,
        episode: Episode,
        quality: Int,
        playFlag: Int? = null
    ) {
        viewModel.submitPlayerOpenAnalytics(
            PreferencesHolder.PLAYER_TYPE_INTERNAL.toAnalyticsPlayer(),
            quality.toPrefQuality().toAnalyticsQuality()
        )
        startActivity(Intent(context, MyPlayerActivity::class.java).apply {
            putExtra(MyPlayerActivity.ARG_RELEASE, release)
            putExtra(MyPlayerActivity.ARG_EPISODE_ID, episode.id)
            putExtra(MyPlayerActivity.ARG_QUALITY, quality)
            playFlag?.let {
                putExtra(MyPlayerActivity.ARG_PLAY_FLAG, it)
            }
        })
    }

    private fun playExternal(release: Release, episode: Episode, quality: Int) {
        viewModel.submitPlayerOpenAnalytics(
            PreferencesHolder.PLAYER_TYPE_EXTERNAL.toAnalyticsPlayer(),
            quality.toPrefQuality().toAnalyticsQuality()
        )
        viewModel.markEpisodeViewed(episode)
        val url = when (quality) {
            MyPlayerActivity.VAL_QUALITY_SD -> episode.urlSd
            MyPlayerActivity.VAL_QUALITY_HD -> episode.urlHd
            MyPlayerActivity.VAL_QUALITY_FULL_HD -> episode.urlFullHd
            else -> episode.urlSd
        }
        val fileUri = Uri.parse(url)
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(fileUri, URLConnection.guessContentTypeFromName(fileUri.toString()))
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Ничего не найдено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playWeb(link: String, code: String) {
        viewModel.onWebPlayerClick()
        startActivity(Intent(context, WebPlayerActivity::class.java).apply {
            putExtra(WebPlayerActivity.ARG_URL, link)
            putExtra(WebPlayerActivity.ARG_RELEASE_CODE, code)
        })
    }

    private fun <T> selectQuality(
        qualityInfo: QualityInfo<T>,
        onSelect: (quality: Int) -> Unit,
        forceDialog: Boolean = false
    ) {
        val savedQuality = viewModel.getQuality()

        var correctQuality = savedQuality
        if (correctQuality == PreferencesHolder.QUALITY_FULL_HD && !qualityInfo.hasFullHd) {
            correctQuality = PreferencesHolder.QUALITY_HD
        }
        if (correctQuality == PreferencesHolder.QUALITY_HD && !qualityInfo.hasHd) {
            correctQuality = PreferencesHolder.QUALITY_SD
        }
        if (correctQuality == PreferencesHolder.QUALITY_SD && !qualityInfo.hasSd) {
            correctQuality = PreferencesHolder.QUALITY_NO
        }

        when {
            correctQuality != savedQuality -> showQualityDialog(qualityInfo, onSelect, false)
            forceDialog -> showQualityDialog(qualityInfo, onSelect, false)
            else -> when (savedQuality) {
                PreferencesHolder.QUALITY_NO -> showQualityDialog(qualityInfo, onSelect)
                PreferencesHolder.QUALITY_ALWAYS -> showQualityDialog(qualityInfo, onSelect, false)
                PreferencesHolder.QUALITY_SD -> onSelect(MyPlayerActivity.VAL_QUALITY_SD)
                PreferencesHolder.QUALITY_HD -> onSelect(MyPlayerActivity.VAL_QUALITY_HD)
                PreferencesHolder.QUALITY_FULL_HD -> onSelect(MyPlayerActivity.VAL_QUALITY_FULL_HD)
            }
        }
    }

    private fun <T> showQualityDialog(
        qualityInfo: QualityInfo<T>,
        onSelect: (quality: Int) -> Unit,
        saveQuality: Boolean = true
    ) {
        val context = context ?: return

        val qualities = mutableListOf<Int>()
        if (qualityInfo.hasSd) qualities.add(MyPlayerActivity.VAL_QUALITY_SD)
        if (qualityInfo.hasHd) qualities.add(MyPlayerActivity.VAL_QUALITY_HD)
        if (qualityInfo.hasFullHd) qualities.add(MyPlayerActivity.VAL_QUALITY_FULL_HD)

        val titles = qualities
            .map {
                when (it) {
                    MyPlayerActivity.VAL_QUALITY_SD -> "480p"
                    MyPlayerActivity.VAL_QUALITY_HD -> "720p"
                    MyPlayerActivity.VAL_QUALITY_FULL_HD -> "1080p"
                    else -> "Unknown"
                }
            }
            .toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("Качество")
            .setItems(titles) { _, p1 ->
                val quality = qualities[p1]
                if (quality != -1) {
                    if (saveQuality) {
                        viewModel.setQuality(
                            when (quality) {
                                MyPlayerActivity.VAL_QUALITY_SD -> PreferencesHolder.QUALITY_SD
                                MyPlayerActivity.VAL_QUALITY_HD -> PreferencesHolder.QUALITY_HD
                                MyPlayerActivity.VAL_QUALITY_FULL_HD -> PreferencesHolder.QUALITY_FULL_HD
                                else -> PreferencesHolder.QUALITY_NO
                            }
                        )
                    }
                    onSelect.invoke(quality)
                }
            }
            .show()
    }

    private fun showFavoriteDialog() {
        val context = context ?: return
        AlertDialog.Builder(context)
            .setMessage("Для выполнения действия необходимо авторизоваться. Авторизоваться?")
            .setPositiveButton("Да") { _, _ -> viewModel.openAuth() }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun showTorrentInfoDialog() {
        TorrentInfoDialogFragment().show(childFragmentManager, "torrents")
    }

    private val headListener = object : ReleaseHeadDelegate.Listener {

        override fun onClickSomeLink(url: String) {
            viewModel.onClickLink(url)
        }

        override fun onClickGenre(tag: String, index: Int) {
            viewModel.openSearch(tag, index)
        }

        override fun onClickFav() {
            viewModel.onClickFav()
        }

        override fun onScheduleClick(day: Int) {
            viewModel.onScheduleClick(day)
        }

        override fun onExpandClick() {
            viewModel.onDescriptionExpandClick()
        }
    }

    private val episodeListener = object : ReleaseEpisodeDelegate.Listener {

        override fun onClickSd(episode: ReleaseEpisodeItemState) {
            viewModel.onEpisodeClick(
                episode,
                MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE,
                MyPlayerActivity.VAL_QUALITY_SD
            )
        }

        override fun onClickHd(episode: ReleaseEpisodeItemState) {
            viewModel.onEpisodeClick(
                episode,
                MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE,
                MyPlayerActivity.VAL_QUALITY_HD
            )
        }

        override fun onClickFullHd(episode: ReleaseEpisodeItemState) {
            viewModel.onEpisodeClick(
                episode,
                MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE,
                MyPlayerActivity.VAL_QUALITY_FULL_HD
            )
        }

        override fun onClickEpisode(episode: ReleaseEpisodeItemState) {
            viewModel.onEpisodeClick(episode, MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE)
        }

        override fun onLongClickEpisode(episode: ReleaseEpisodeItemState) {
            viewModel.onLongClickEpisode(episode)
        }
    }

    private val episodeControlListener = object : ReleaseEpisodeControlDelegate.Listener {

        override fun onClickWatchWeb(place: EpisodeControlPlace) {
            viewModel.onClickWatchWeb(place)
        }

        override fun onClickWatchAll(place: EpisodeControlPlace) {
            viewModel.onPlayAllClick(place)
        }

        override fun onClickContinue(place: EpisodeControlPlace) {
            viewModel.onClickContinue(place)
        }

        override fun onClickEpisodesMenu(place: EpisodeControlPlace) {
            viewModel.onClickEpisodesMenu(place)
        }
    }

    data class QualityInfo<T>(
        val data: T,
        val urlSd: String?,
        val urlHd: String?,
        val urlFullHd: String?
    ) {
        val hasSd = urlSd != null
        val hasHd = urlHd != null
        val hasFullHd = urlFullHd != null
    }

}