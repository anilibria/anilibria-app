package ru.radiationx.anilibria.ui.fragments.release.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.ui.adapters.release.detail.EpisodeControlPlace
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseEpisodeControlDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseEpisodeDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseHeadDelegate
import ru.radiationx.anilibria.ui.fragments.BaseDimensionsFragment
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.release.SourceEpisode
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.launchInResumed
import ru.radiationx.shared.ktx.android.showWithLifecycle
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.imageloader.showImageUrl

class ReleaseInfoFragment : BaseDimensionsFragment(R.layout.fragment_list) {

    private val releaseInfoAdapter: ReleaseInfoAdapter by lazy {
        ReleaseInfoAdapter(
            headListener = headListener,
            episodeListener = episodeListener,
            episodeControlListener = episodeControlListener,
            donationListener = { viewModel.onClickDonate() },
            donationCloseListener = {},
            torrentClickListener = viewModel::onTorrentClick,
            torrentCancelClickListener = viewModel::onCancelTorrentClick,
            commentsClickListener = viewModel::onCommentsClick,
            episodesTabListener = viewModel::onEpisodeTabClick,
            remindCloseListener = viewModel::onRemindCloseClick,
            torrentInfoListener = { showTorrentInfoDialog() }
        )
    }

    private val systemUtils by inject<SystemUtils>()

    private val viewModel by viewModel<ReleaseInfoViewModel>()

    private val binding by viewBinding<FragmentListBinding>()

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

        viewModel.playEpisodesAction.observe().onEach {
            it.episodes.lastOrNull()?.also { episode ->
                playEpisode(episode)
            }
        }.launchInResumed(viewLifecycleOwner)

        viewModel.playContinueAction.observe().onEach {
            playEpisode(it.startWith)
        }.launchInResumed(viewLifecycleOwner)

        viewModel.playWebAction.observe().onEach {
            playWeb(it.link, it.code)
        }.launchInResumed(viewLifecycleOwner)

        viewModel.playEpisodeAction.observe().onEach {
            playEpisode(it.episode)
        }.launchInResumed(viewLifecycleOwner)

        viewModel.showUnauthAction.observe().onEach {
            showFavoriteDialog()
        }.launchInResumed(viewLifecycleOwner)

        viewModel.showFileDonateAction.observe().onEach {
            showFileDonateDialog(it)
        }.launchInResumed(viewLifecycleOwner)

        viewModel.showEpisodesMenuAction.observe().onEach {
            showEpisodesMenuDialog()
        }.launchInResumed(viewLifecycleOwner)

        viewModel.showContextEpisodeAction.observe().onEach {
            showLongPressEpisodeDialog(it)
        }.launchInResumed(viewLifecycleOwner)

        viewModel.openDownloadedFileAction.observe().onEach {
            systemUtils.openRemoteFile(it.local, it.remote.name, it.remote.mimeType)
        }.launchInResumed(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
    }

    private fun showState(state: ReleaseDetailScreenState) {
        state.data?.let { releaseInfoAdapter.bindState(it, state) }
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
            .showWithLifecycle(viewLifecycleOwner)

        dialogBinding.dialogFilePatreonBtn.setOnClickListener {
            viewModel.onDialogPatreonClick()
            dialog.dismiss()
        }
        dialogBinding.dialogFileDonateBtn.setOnClickListener {
            viewModel.onDialogDonateClick()
            dialog.dismiss()
        }
        dialogBinding.dialogFileDownloadBtn.setOnClickListener {
            viewModel.downloadFile(url)
            dialog.dismiss()
        }
    }

    private fun showEpisodesMenuDialog() {
        val items = arrayOf(
            "Сбросить историю просмотров",
            "Отметить все как просмотренные"
        )
        AlertDialog.Builder(requireContext())
            .setItems(items) { _, which ->
                when (which) {
                    0 -> viewModel.onResetEpisodesHistoryClick()
                    1 -> viewModel.onCheckAllEpisodesHistoryClick()
                }
            }
            .showWithLifecycle(viewLifecycleOwner)
    }

    private fun showLongPressEpisodeDialog(episode: Episode) {
        val items = arrayOf(
            "Отметить как непросмотренная"
        )
        AlertDialog.Builder(requireContext())
            .setItems(items) { _, which ->
                when (which) {
                    0 -> viewModel.markEpisodeUnviewed(episode)
                }
            }
            .showWithLifecycle(viewLifecycleOwner)
    }

    private fun playEpisode(episode: Episode) {
        viewModel.submitPlayerOpenAnalytics()
        val intent = Screens.Player(episode.id).getActivityIntent(requireContext())
        startActivity(intent)
    }

    private fun playWeb(link: String, code: String) {
        viewModel.onWebPlayerClick()
        val intent = Screens.WebPlayer(link, code).getActivityIntent(requireContext())
        startActivity(intent)
    }

    private fun showFavoriteDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Для выполнения действия необходимо авторизоваться. Авторизоваться?")
            .setPositiveButton("Да") { _, _ -> viewModel.openAuth() }
            .setNegativeButton("Нет", null)
            .showWithLifecycle(viewLifecycleOwner)
    }

    private fun showTorrentInfoDialog() {
        TorrentInfoDialogFragment().show(childFragmentManager, "torrents")
    }

    private val headListener = object : ReleaseHeadDelegate.Listener {

        override fun onClickSomeLink(url: String) {
            viewModel.onClickLink(url)
        }

        override fun onClickGenre(tag: String, value: String) {
            viewModel.openSearch(tag, value)
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

        override fun onClickEpisode(episode: ReleaseEpisodeItemState, quality: PlayerQuality?) {
            viewModel.onEpisodeClick(episode, quality)
        }

        override fun onLongClickEpisode(episode: ReleaseEpisodeItemState) {
            viewModel.onLongClickEpisode(episode)
        }
    }

    private val episodeControlListener = object : ReleaseEpisodeControlDelegate.Listener {

        override fun onClickWatchWeb(place: EpisodeControlPlace) {
            viewModel.onClickWatchWeb()
        }

        override fun onClickWatchAll(place: EpisodeControlPlace) {
            viewModel.onPlayAllClick(place)
        }

        override fun onClickContinue(place: EpisodeControlPlace) {
            viewModel.onClickContinue(place)
        }

        override fun onClickEpisodesMenu(place: EpisodeControlPlace) {
            viewModel.onClickEpisodesMenu()
        }
    }

}