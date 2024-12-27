package ru.radiationx.anilibria.ui.fragments.release.details

import android.os.Bundle
import android.view.View
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
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.TorrentId
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.launchInResumed
import ru.radiationx.shared_app.common.SystemUtils
import taiwa.TaiwaAction
import taiwa.alert.alert
import taiwa.bottomsheet.bottomSheetTaiwa

class ReleaseInfoFragment : BaseDimensionsFragment(R.layout.fragment_list), TopScroller {

    private val releaseInfoAdapter: ReleaseInfoAdapter by lazy {
        ReleaseInfoAdapter(
            headListener = headListener,
            episodeListener = episodeListener,
            episodeControlListener = episodeControlListener,
            donationListener = { viewModel.onClickDonate() },
            donationCloseListener = {},
            torrentClickListener = { showTorrentDialog(it) },
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


    private val episodeMenuTaiwa by bottomSheetTaiwa {
        items {
            action(TaiwaAction.Close)
            item {
                icon(R.drawable.ic_baseline_done_all_24)
                title("Отметить все как просмотренные")
                onClick { viewModel.onCheckAllEpisodesHistoryClick() }
            }
            item {
                icon(R.drawable.ic_baseline_remove_done_24)
                title("Сбросить историю просмотров")
                tint(androidx.appcompat.R.attr.colorError)
                onClick { viewModel.onResetEpisodesHistoryClick() }
            }
        }
    }

    private val episodeTaiwa by bottomSheetTaiwa()

    private val torrentTaiwa by bottomSheetTaiwa()

    private val favoriteTaiwa by bottomSheetTaiwa {
        message {
            text("Для выполнения действия необходимо авторизоваться. Авторизоваться?")
        }
        buttons {
            action(TaiwaAction.Close)
            button {
                text("Да")
                onClick { viewModel.openAuth() }
            }
            button {
                text("Нет")
            }
        }
    }

    private val fileDonateAlert by alert()

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

        viewModel.playWebAction.observe().onEach {
            playWeb(it.link, it.code)
        }.launchInResumed(viewLifecycleOwner)

        viewModel.playEpisodeAction.observe().onEach {
            playEpisode(it.id)
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
            systemUtils.openLocalFile(it)
        }.launchInResumed(viewLifecycleOwner)

        viewModel.shareDownloadedFileAction.observe().onEach {
            systemUtils.shareLocalFile(it)
        }.launchInResumed(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
    }

    override fun scrollToTop() {
        binding.recyclerView.scrollToPosition(0)
    }

    private fun showState(state: ReleaseDetailScreenState) {
        state.data?.let { releaseInfoAdapter.bindState(it, state) }
    }

    private fun showFileDonateDialog(url: String) {
        val dialogBinding = fileDonateAlert.setContentBinding {
            DialogFileDownloadBinding.inflate(it, null, false)
        }
        dialogBinding.dialogFilePatreonBtn.setOnClickListener {
            viewModel.onDialogPatreonClick()
            fileDonateAlert.close()
        }
        dialogBinding.dialogFileDonateBtn.setOnClickListener {
            viewModel.onDialogDonateClick()
            fileDonateAlert.close()
        }
        dialogBinding.dialogFileDownloadBtn.setOnClickListener {
            viewModel.downloadFile(url)
            fileDonateAlert.close()
        }
        fileDonateAlert.show()
    }

    private fun showEpisodesMenuDialog() {
        episodeMenuTaiwa.show()
    }

    private fun showLongPressEpisodeDialog(episode: Episode) {
        episodeTaiwa.setContent {
            items {
                action(TaiwaAction.Close)
                item {
                    icon(R.drawable.ic_baseline_remove_done_24)
                    title("Отметить как непросмотренная")
                    tint(androidx.appcompat.R.attr.colorError)
                    onClick { viewModel.markEpisodeUnviewed(episode) }
                }
            }
        }
        episodeTaiwa.show()
    }

    private fun showTorrentDialog(id: TorrentId) {
        torrentTaiwa.setContent {
            items {
                action(TaiwaAction.Close)
                item {
                    icon(R.drawable.ic_outline_file_open_24)
                    title("Открыть файл")
                    onClick { viewModel.onTorrentClick(id, TorrentAction.Open) }
                }
                item {
                    icon(R.drawable.ic_baseline_share_24)
                    title("Поделиться файлом")
                    onClick { viewModel.onTorrentClick(id, TorrentAction.Share) }
                }
                item {
                    icon(R.drawable.ic_baseline_open_in_new_24)
                    title("Открыть ссылку на файл")
                    onClick { viewModel.onTorrentClick(id, TorrentAction.OpenUrl) }
                }
                item {
                    icon(R.drawable.ic_baseline_share_24)
                    title("Поделиться ссылкой на файл")
                    onClick { viewModel.onTorrentClick(id, TorrentAction.ShareUrl) }
                }
            }
        }
        torrentTaiwa.show()
    }

    private fun playEpisode(id: EpisodeId) {
        viewModel.submitPlayerOpenAnalytics(id)
        val intent = Screens.Player(id).createIntent(requireContext())
        startActivity(intent)
    }

    private fun playWeb(link: String, code: String) {
        viewModel.onWebPlayerClick()
        val intent = Screens.WebPlayer(link, code).createIntent(requireContext())
        startActivity(intent)
    }

    private fun showFavoriteDialog() {
        favoriteTaiwa.show()
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