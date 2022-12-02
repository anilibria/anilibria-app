package ru.radiationx.anilibria.ui.fragments.release.details

import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.DialogTorrentsInfoBinding
import ru.radiationx.anilibria.ui.fragments.AlertDialogFragment
import ru.radiationx.quill.quillGet
import ru.radiationx.shared_app.common.SystemUtils

class TorrentInfoDialogFragment : AlertDialogFragment(R.layout.dialog_torrents_info) {

    private val binding by viewBinding<DialogTorrentsInfoBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btSearch.setOnClickListener {
            quillGet<SystemUtils>()
                .externalLink("https://play.google.com/store/search?q=torrent&c=apps")
        }
        binding.btCancel.setOnClickListener {
            dismiss()
        }
    }
}