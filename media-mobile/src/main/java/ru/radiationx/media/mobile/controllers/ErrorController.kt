package ru.radiationx.media.mobile.controllers

import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.radiationx.media.mobile.PlayerFlow
import ru.radiationx.media.mobile.holder.PlayerAttachListener

class ErrorController(
    private val coroutineScope: CoroutineScope,
    private val playerFlow: PlayerFlow,
    private val errorMessageText: TextView,
    private val errorButtonAction: Button,
) : PlayerAttachListener {

    init {
        errorButtonAction.setOnClickListener {
            playerFlow.play()
        }
        playerFlow.playerState
            .map { it.errorMessage }
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { errorMessageText.text = it }
            .launchIn(coroutineScope)
    }
}