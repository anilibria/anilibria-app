package ru.radiationx.anilibria.ui.fragments.release.details

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.dialog_file_download.view.*
import kotlinx.android.synthetic.main.fragment_list.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.data.entity.app.release.TorrentItem
import ru.radiationx.data.entity.app.vital.VitalItem
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.anilibria.presentation.release.details.ReleaseInfoPresenter
import ru.radiationx.anilibria.presentation.release.details.ReleaseInfoView
import ru.radiationx.anilibria.ui.activities.MyPlayerActivity
import ru.radiationx.anilibria.ui.activities.WebPlayerActivity
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.utils.Utils
import java.net.URLConnection
import java.text.DecimalFormat
import java.util.regex.Pattern

@RuntimePermissions
class ReleaseInfoFragment : BaseFragment(), ReleaseInfoView {

    companion object {
        const val ARG_ID: String = "release_id"
        const val ARG_ID_CODE: String = "release_id_code"
    }

    private val releaseInfoAdapter: ReleaseInfoAdapter by lazy {
        ReleaseInfoAdapter(adapterListener) {
            loadTorrent(it)
        }
    }

    @InjectPresenter
    lateinit var presenter: ReleaseInfoPresenter

    @ProvidePresenter
    fun provideReleasePresenter(): ReleaseInfoPresenter = getDependency(screenScope, ReleaseInfoPresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        Log.e("S_DEF_LOG", "ONCRETE $this")
        Log.e("S_DEF_LOG", "ONCRETE REL $arguments, $savedInstanceState")
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

    override fun setRefreshing(refreshing: Boolean) {}

    override fun showRelease(release: ReleaseFull) {
        releaseInfoAdapter.setRelease(release)
    }

    override fun loadTorrent(torrent: TorrentItem) {
        torrent.url?.also { Utils.externalLink(it) }
    }

    override fun showTorrentDialog(torrents: List<TorrentItem>) {
        val context = context ?: return
        val titles = torrents.map { "Серия ${it.series} [${it.quality}][${readableFileSize(it.size)}]" }.toTypedArray()
        AlertDialog.Builder(context)
                .setItems(titles) { dialog, which ->
                    loadTorrent(torrents[which])
                }
                .show()
    }

    private fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    override fun playEpisodes(release: ReleaseFull) {
        playEpisode(release, release.episodes.last())
    }

    override fun playContinue(release: ReleaseFull, startWith: ReleaseFull.Episode) {
        playEpisode(release, startWith, MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE)
    }

    private fun getUrlByQuality(episode: ReleaseFull.Episode, quality: Int): String {
        return when (quality) {
            MyPlayerActivity.VAL_QUALITY_SD -> episode.urlSd
            MyPlayerActivity.VAL_QUALITY_HD -> episode.urlHd
            MyPlayerActivity.VAL_QUALITY_FULL_HD -> episode.urlFullHd
            else -> episode.urlSd
        }.orEmpty()
    }

    override fun showDownloadDialog(url: String) {
        val context = context ?: return
        val titles = arrayOf("Внешний загрузчик", "Системный загрузчик")
        AlertDialog.Builder(context)
                .setItems(titles) { _, which ->
                    when (which) {
                        0 -> Utils.externalLink(url)
                        1 -> systemDownloadWithPermissionCheck(url)
                    }
                }
                .show()
    }

    override fun showFileDonateDialog(url: String) {
        val dialogView = LayoutInflater.from(view!!.context)
                .inflate(R.layout.dialog_file_download, null, false)
                .apply {
                    layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

        ImageLoader.getInstance().displayImage("assets://libria_tyan_type3.png", dialogView.dialogFileImage)

        val dialog = AlertDialog.Builder(context!!)
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

    override fun playEpisode(release: ReleaseFull, episode: ReleaseFull.Episode, playFlag: Int?, quality: Int?) {
        if (episode.type == ReleaseFull.Episode.Type.SOURCE) {
            if (quality == null) {
                selectQuality(episode, { selected ->
                    presenter.onDownloadLinkSelected(getUrlByQuality(episode, selected))
                }, true)
            } else {
                presenter.onDownloadLinkSelected(getUrlByQuality(episode, quality))
            }
        } else {
            selectPlayer({ playerType ->
                if (quality == null) {
                    when (playerType) {
                        PreferencesHolder.PLAYER_TYPE_EXTERNAL -> {
                            selectQuality(episode, { selected ->
                                playExternal(release, episode, selected)
                            }, true)
                        }
                        PreferencesHolder.PLAYER_TYPE_INTERNAL -> {
                            selectQuality(episode, { selected ->
                                playInternal(release, episode, selected, playFlag)
                            })
                        }
                    }
                } else {
                    when (playerType) {
                        PreferencesHolder.PLAYER_TYPE_EXTERNAL -> playExternal(release, episode, quality)
                        PreferencesHolder.PLAYER_TYPE_INTERNAL -> playInternal(release, episode, quality, playFlag)
                    }
                }
            })
        }
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

    private fun showSelectPlayerDialog(onSelect: (playerType: Int) -> Unit, savePlayerType: Boolean = true) {
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
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun playInternal(release: ReleaseFull, episode: ReleaseFull.Episode, quality: Int, playFlag: Int? = null) {
        startActivity(Intent(context, MyPlayerActivity::class.java).apply {
            putExtra(MyPlayerActivity.ARG_RELEASE, release)
            putExtra(MyPlayerActivity.ARG_EPISODE_ID, episode.id)
            putExtra(MyPlayerActivity.ARG_QUALITY, quality)
            playFlag?.let {
                putExtra(MyPlayerActivity.ARG_PLAY_FLAG, it)
            }
        })
    }

    private fun playExternal(release: ReleaseFull, episode: ReleaseFull.Episode, quality: Int) {
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
        startActivity(Intent(context, WebPlayerActivity::class.java).apply {
            putExtra(WebPlayerActivity.ARG_URL, link)
            putExtra(WebPlayerActivity.ARG_RELEASE_CODE, code)
        })
    }

    private fun selectQuality(episode: ReleaseFull.Episode, onSelect: (quality: Int) -> Unit, forceDialog: Boolean = false) {
        val savedQuality = presenter.getQuality()

        var correctQuality = savedQuality
        if (correctQuality == PreferencesHolder.QUALITY_FULL_HD && episode.urlFullHd == null) {
            correctQuality = PreferencesHolder.QUALITY_HD
        }
        if (correctQuality == PreferencesHolder.QUALITY_HD && episode.urlHd == null) {
            correctQuality = PreferencesHolder.QUALITY_SD
        }
        if (correctQuality == PreferencesHolder.QUALITY_SD && episode.urlSd == null) {
            correctQuality = PreferencesHolder.QUALITY_NO
        }

        when {
            correctQuality != savedQuality -> showQualityDialog(episode, onSelect, false)
            forceDialog -> showQualityDialog(episode, onSelect, false)
            else -> when (savedQuality) {
                PreferencesHolder.QUALITY_NO -> showQualityDialog(episode, onSelect)
                PreferencesHolder.QUALITY_ALWAYS -> showQualityDialog(episode, onSelect, false)
                PreferencesHolder.QUALITY_SD -> onSelect(MyPlayerActivity.VAL_QUALITY_SD)
                PreferencesHolder.QUALITY_HD -> onSelect(MyPlayerActivity.VAL_QUALITY_HD)
                PreferencesHolder.QUALITY_FULL_HD -> onSelect(MyPlayerActivity.VAL_QUALITY_FULL_HD)
            }
        }
    }

    private fun showQualityDialog(episode: ReleaseFull.Episode, onSelect: (quality: Int) -> Unit, saveQuality: Boolean = true) {
        val context = context ?: return

        val qualities = mutableListOf<Int>()
        if (episode.urlSd != null) qualities.add(MyPlayerActivity.VAL_QUALITY_SD)
        if (episode.urlHd != null) qualities.add(MyPlayerActivity.VAL_QUALITY_HD)
        if (episode.urlFullHd != null) qualities.add(MyPlayerActivity.VAL_QUALITY_FULL_HD)

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
                            presenter.setQuality(when (quality) {
                                MyPlayerActivity.VAL_QUALITY_SD -> PreferencesHolder.QUALITY_SD
                                MyPlayerActivity.VAL_QUALITY_HD -> PreferencesHolder.QUALITY_HD
                                MyPlayerActivity.VAL_QUALITY_FULL_HD -> PreferencesHolder.QUALITY_FULL_HD
                                else -> PreferencesHolder.QUALITY_NO
                            })
                        }
                        onSelect.invoke(quality)
                    }
                }
                .show()
    }

    override fun updateFavCounter() {
        releaseInfoAdapter.notifyDataSetChanged()
    }

    override fun showVitalItems(vital: List<VitalItem>) {
        releaseInfoAdapter.setVitals(vital)
    }

    override fun showFavoriteDialog() {
        val context = context ?: return
        AlertDialog.Builder(context)
                .setMessage("Для выполнения действия необходимо авторизоваться. Авторизоваться?")
                .setPositiveButton("Да") { _, _ -> presenter.openAuth() }
                .setNegativeButton("Нет", null)
                .show()
    }

    private val adapterListener = object : ReleaseInfoAdapter.ItemListener {

        override fun onClickSd(episode: ReleaseFull.Episode) {
            presenter.onPlayEpisodeClick(episode, MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE, MyPlayerActivity.VAL_QUALITY_SD)
        }

        override fun onClickHd(episode: ReleaseFull.Episode) {
            presenter.onPlayEpisodeClick(episode, MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE, MyPlayerActivity.VAL_QUALITY_HD)
        }

        override fun onClickFullHd(episode: ReleaseFull.Episode) {
            presenter.onPlayEpisodeClick(episode, MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE, MyPlayerActivity.VAL_QUALITY_FULL_HD)
        }

        override fun onClickEpisode(episode: ReleaseFull.Episode) {
            presenter.onPlayEpisodeClick(episode, MyPlayerActivity.PLAY_FLAG_FORCE_CONTINUE)
        }

        override fun onClickTorrent() {
            presenter.onTorrentClick()
        }

        override fun onClickWatchAll() {
            presenter.onPlayAllClick()
        }

        override fun onClickContinue() {
            presenter.onClickContinue()
        }

        override fun onClickEpisodesMenu() {
            presenter.onClickEpisodesMenu()
        }

        override fun onClickTag(text: String) {
            presenter.openSearch(text)
        }

        override fun onClickFav() {
            presenter.onClickFav()
        }

        override fun onClickSomeLink(url: String): Boolean {
            val handled = presenter.onClickLink(url)
            if (!handled) {
                Utils.externalLink(url)
            }
            return true
        }

        override fun onClickDonate() {
            presenter.onClickDonate()
        }

        override fun onClickWatchWeb() {
            presenter.onClickWatchWeb()
        }

        override fun onScheduleClick(day: Int) {
            presenter.onScheduleClick(day)
        }
    }
}