package ru.radiationx.shared_app.networkstatus

import ru.radiationx.shared_app.R


fun NetworkStatusState.toViewState(): NetworkStatusViewState {
    val text = when (this) {
        NetworkStatusState.NotConnected -> "Нет подключения"
        NetworkStatusState.Connected -> "Подключение восстановлено"
        NetworkStatusState.Configuring -> "Обновление конфигурации"
        NetworkStatusState.Configured -> "Конфигурация обновлена"
    }
    val colorRes = when (this) {
        NetworkStatusState.NotConnected -> R.color.md_red
        NetworkStatusState.Connected -> R.color.md_green
        NetworkStatusState.Configuring -> R.color.md_orange
        NetworkStatusState.Configured -> R.color.md_green
    }
    val visible = when (this) {
        NetworkStatusState.NotConnected,
        NetworkStatusState.Configuring -> true

        NetworkStatusState.Connected,
        NetworkStatusState.Configured -> false
    }
    return NetworkStatusViewState(text, colorRes, visible)
}