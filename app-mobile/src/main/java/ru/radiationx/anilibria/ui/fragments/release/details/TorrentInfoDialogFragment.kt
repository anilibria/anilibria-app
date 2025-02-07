package ru.radiationx.anilibria.ui.fragments.release.details

import android.os.Bundle
import android.view.View
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.DialogTorrentsInfoBinding
import ru.radiationx.quill.get
import ru.radiationx.shared_app.common.SystemUtils
import taiwa.dialogs.TaiwaDialogFragment

class TorrentInfoDialogFragment : TaiwaDialogFragment(R.layout.dialog_torrents_info) {

    private val binding by viewBinding<DialogTorrentsInfoBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btSearch.setOnClickListener {
            get<SystemUtils>()
                .externalLink("https://play.google.com/store/search?q=torrent&c=apps")
        }
        binding.btCancel.setOnClickListener {
            dismiss()
        }
    }
}