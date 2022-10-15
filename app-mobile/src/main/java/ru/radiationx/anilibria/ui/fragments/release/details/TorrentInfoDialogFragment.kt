package ru.radiationx.anilibria.ui.fragments.release.details

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_torrents_info.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.fragments.AlertDialogFragment
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.di.DI

class TorrentInfoDialogFragment : AlertDialogFragment(R.layout.dialog_torrents_info) {

    override fun onStart() {
        super.onStart()
        getAlertDialog()?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btSearch.setOnClickListener {
            DI.get(SystemUtils::class.java)
                .externalLink("https://play.google.com/store/search?q=torrent&c=apps")
        }
        btCancel.setOnClickListener {
            dismiss()
        }
    }
}