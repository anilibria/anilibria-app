package ru.radiationx.anilibria.ui.fragments.release.details

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.dialog_file_download.view.*
import kotlinx.android.synthetic.main.fragment_list.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.disableItemChangeAnimation
import ru.radiationx.anilibria.presentation.release.details.ReleaseDetailScreenState
import ru.radiationx.anilibria.presentation.release.details.ReleaseEpisodeItemState
import ru.radiationx.anilibria.presentation.release.details.ReleaseInfoPresenter
import ru.radiationx.anilibria.presentation.release.details.ReleaseInfoView
import ru.radiationx.anilibria.ui.activities.MyPlayerActivity
import ru.radiationx.anilibria.ui.activities.WebPlayerActivity
import ru.radiationx.anilibria.ui.activities.toPrefQuality
import ru.radiationx.anilibria.ui.adapters.release.detail.EpisodeControlPlace
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseEpisodeControlDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseEpisodeDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseHeadDelegate
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.features.mapper.toAnalyticsPlayer
import ru.radiationx.data.analytics.features.mapper.toAnalyticsQuality
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.app.release.Episode
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.SourceEpisode
import ru.radiationx.data.entity.app.release.TorrentItem
import ru.radiationx.shared_app.di.injectDependencies
import java.net.URLConnection
import java.util.regex.Pattern

@RuntimePermissions
class ReleaseInfoFragment : BaseFragment(), ReleaseInfoView {

    companion object {
        const val ARG_ID: String = "release_id"
        const val ARG_ID_CODE: String = "release_id_code"
    }

    private val releaseInfoAdapter: ReleaseInfoAdapter by lazy {
        ReleaseInfoAdapter(
            headListener = headListener,
            episodeListener = episodeListener,
            episodeControlListener = episodeControlListener,
            donationListener = { presenter.onClickDonate() },
            donationCloseListener = {},
            torrentClickListener = presenter::onTorrentClick,
            commentsClickListener = presenter::onCommentsClick,
            episodesTabListener = presenter::onEpisodeTabClick,
            remindCloseListener = presenter::onRemindCloseClick,
            torrentInfoListener = { showTorrentInfoDialog() }
        )
    }

    @InjectPresenter
    lateinit var presenter: ReleaseInfoPresenter

    @ProvidePresenter
    fun provideReleasePresenter(): ReleaseInfoPresenter =
        getDependency(ReleaseInfoPresenter::class.java, screenScope)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.also { bundle ->
            presenter.releaseId = bundle.getInt(ARG_ID, presenter.releaseId)
            presenter.releaseIdCode = bundle.getString(ARG_ID_CODE, presenter.releaseIdCode)
        }
    }

    override fun getBaseLayout(): Int = R.layout.fragment_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = releaseInfoAdapter
            setHasFixedSize(true)
            disableItemChangeAnimation()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ARG_ID, presenter.releaseId)
        outState.putString(ARG_ID_CODE, presenter.releaseIdCode)
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun showState(state: ReleaseDetailScreenState) {
        state.data?.let { releaseInfoAdapter.bindState(it, state) }
    }

    override fun loadTorrent(torrent: TorrentItem) {
        torrent.url?.also { Utils.externalLink(it) }
    }

    override fun showTorrentDialog(torrents: List<TorrentItem>) {
        val context = context ?: return
        val titles =
            torrents.map { "Серия ${it.series} [${it.quality}][${Utils.readableFileSize(it.size)}]" }
                .toTypedArray()
        AlertDialog.Builder(context)
            .setItems(titles) { dialog, which ->
                loadTorrent(torrents[which])
            }
            .show()
    }

    override fun playEpisodes(release: ReleaseFull) {
        playEpisode(release, release.episodes.last())
    }

    override fun playContinue(release: ReleaseFull, startWith: Episode) {
        playEpisode(release, startWith, MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE)
    }

    private fun <T> getUrlByQuality(qualityInfo: QualityInfo<T>, quality: Int): String {
        return when (quality) {
            MyPlayerActivity.VAL_QUALITY_SD -> qualityInfo.urlSd
            MyPlayerActivity.VAL_QUALITY_HD -> qualityInfo.urlHd
            MyPlayerActivity.VAL_QUALITY_FULL_HD -> qualityInfo.urlFullHd
            else -> qualityInfo.urlSd
        }.orEmpty()
    }

    override fun showDownloadDialog(url: String) {
        val context = context ?: return
        val titles = arrayOf("Внешний загрузчик", "Системный загрузчик")
        AlertDialog.Builder(context)
            .setItems(titles) { _, which ->
                presenter.submitDownloadEpisodeUrlAnalytics()
                when (which) {
                    0 -> Utils.externalLink(url)
                    1 -> systemDownloadWithPermissionCheck(url)
                }
            }
            .show()
    }

    override fun showFileDonateDialog(url: String) {
        val dialogView = LayoutInflater.from(requireView().context)
            .inflate(R.layout.dialog_file_download, null, false)
            .apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

        ImageLoader.getInstance()
            .displayImage("assets://libria_tyan_type3.png", dialogView.dialogFileImage)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .show()

        dialogView.dialogFilePatreonBtn.setOnClickListener {
            presenter.onDialogPatreonClick()
            dialog.dismiss()
        }
        dialogView.dialogFileDonateBtn.setOnClickListener {
            presenter.onDialogDonateClick()
            dialog.dismiss()
        }
        dialogView.dialogFileDownloadBtn.setOnClickListener {
            showDownloadDialog(url)
            dialog.dismiss()
        }
    }

    override fun showEpisodesMenuDialog() {
        val context = context ?: return
        val items = arrayOf(
            "Сбросить историю просмотров",
            "Отметить все как просмотренные"
        )
        AlertDialog.Builder(context)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> presenter.onResetEpisodesHistoryClick()
                    1 -> presenter.onCheckAllEpisodesHistoryClick()
                }
            }
            .show()
    }

    override fun showLongPressEpisodeDialog(episode: Episode) {
        val context = context ?: return
        val items = arrayOf(
            "Отметить как непросмотренная"
        )
        AlertDialog.Builder(context)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> presenter.markEpisodeUnviewed(episode)
                }
            }
            .show()
    }

    override fun downloadEpisode(episode: SourceEpisode, quality: Int?) {
        val qualityInfo = QualityInfo(episode, episode.urlSd, episode.urlHd, episode.urlFullHd)
        if (quality == null) {
            selectQuality(qualityInfo, { selected ->
                presenter.onDownloadLinkSelected(getUrlByQuality(qualityInfo, selected))
            }, true)
        } else {
            presenter.onDownloadLinkSelected(getUrlByQuality(qualityInfo, quality))
        }
    }

    override fun playEpisode(
        release: ReleaseFull,
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
            val savedPlayerType = presenter.getPlayerType()
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
                        presenter.setPlayerType(playerType)
                    }
                    onSelect.invoke(playerType)
                }
            }
            .show()
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun systemDownload(url: String) {
        val context = context ?: return
        var fileName = Utils.getFileNameFromUrl(url)
        val matcher = Pattern.compile("\\?download=([\\s\\S]+)").matcher(fileName)
        if (matcher.find()) {
            fileName = matcher.group(1)
        }
        Utils.systemDownloader(context, url, fileName)
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun playInternal(
        release: ReleaseFull,
        episode: Episode,
        quality: Int,
        playFlag: Int? = null
    ) {
        presenter.submitPlayerOpenAnalytics(
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

    private fun playExternal(release: ReleaseFull, episode: Episode, quality: Int) {
        presenter.submitPlayerOpenAnalytics(
            PreferencesHolder.PLAYER_TYPE_EXTERNAL.toAnalyticsPlayer(),
            quality.toPrefQuality().toAnalyticsQuality()
        )
        presenter.markEpisodeViewed(episode)
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

    override fun playWeb(link: String, code: String) {
        presenter.onWebPlayerClick()
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
        val savedQuality = presenter.getQuality()

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
                        presenter.setQuality(
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

    override fun showFavoriteDialog() {
        val context = context ?: return
        AlertDialog.Builder(context)
            .setMessage("Для выполнения действия необходимо авторизоваться. Авторизоваться?")
            .setPositiveButton("Да") { _, _ -> presenter.openAuth() }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun showTorrentInfoDialog() {
        TorrentInfoDialogFragment().show(childFragmentManager, "torrents")
    }

    private val headListener = object : ReleaseHeadDelegate.Listener {

        override fun onClickSomeLink(url: String) {
            presenter.onClickLink(url)
        }

        override fun onClickGenre(tag: String, index: Int) {
            presenter.openSearch(tag, index)
        }

        override fun onClickFav() {
            presenter.onClickFav()
        }

        override fun onScheduleClick(day: Int) {
            presenter.onScheduleClick(day)
        }

        override fun onExpandClick() {
            presenter.onDescriptionExpandClick()
        }
    }

    private val episodeListener = object : ReleaseEpisodeDelegate.Listener {

        override fun onClickSd(episode: ReleaseEpisodeItemState) {
            presenter.onEpisodeClick(
                episode,
                MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE,
                MyPlayerActivity.VAL_QUALITY_SD
            )
        }

        override fun onClickHd(episode: ReleaseEpisodeItemState) {
            presenter.onEpisodeClick(
                episode,
                MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE,
                MyPlayerActivity.VAL_QUALITY_HD
            )
        }

        override fun onClickFullHd(episode: ReleaseEpisodeItemState) {
            presenter.onEpisodeClick(
                episode,
                MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE,
                MyPlayerActivity.VAL_QUALITY_FULL_HD
            )
        }

        override fun onClickEpisode(episode: ReleaseEpisodeItemState) {
            presenter.onEpisodeClick(episode, MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE)
        }

        override fun onLongClickEpisode(episode: ReleaseEpisodeItemState) {
            presenter.onLongClickEpisode(episode)
        }
    }

    private val episodeControlListener = object : ReleaseEpisodeControlDelegate.Listener {

        override fun onClickWatchWeb(place: EpisodeControlPlace) {
            presenter.onClickWatchWeb(place)
        }

        override fun onClickWatchAll(place: EpisodeControlPlace) {
            presenter.onPlayAllClick(place)
        }

        override fun onClickContinue(place: EpisodeControlPlace) {
            presenter.onClickContinue(place)
        }

        override fun onClickEpisodesMenu(place: EpisodeControlPlace) {
            presenter.onClickEpisodesMenu(place)
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