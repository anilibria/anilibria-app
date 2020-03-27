package ru.radiationx.anilibria.screen.player

import android.os.Bundle
import android.util.Log
import android.view.View
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModel

class PlayerFragment : BasePlayerFragment() {

    companion object {

        private const val ARG_ID = "id"

        fun newInstance(releaseId: Int): PlayerFragment = PlayerFragment().putExtra {
            putInt(ARG_ID, releaseId)
        }
    }

    private val viewModel by viewModel<PlayerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        viewModel.argReleaseId = arguments?.getInt(ARG_ID) ?: viewModel.argReleaseId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeTo(viewModel.videoData) {
            Log.e("kokoko", "video data $it")
            playerGlue?.apply {
                title = it.title
                subtitle = it.subtitle
                preparePlayer(it.url)
                seekTo(it.seek)
                play()
            }
        }
    }

}