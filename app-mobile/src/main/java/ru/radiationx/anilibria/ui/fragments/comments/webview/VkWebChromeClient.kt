package ru.radiationx.anilibria.ui.fragments.comments.webview

import android.app.AlertDialog
import android.os.Message
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import ru.radiationx.anilibria.ui.fragments.comments.VkCommentsViewModel

class VkWebChromeClient(
    private val viewModel: VkCommentsViewModel,
) : WebChromeClient() {

    private val jsErrorRegex = Regex("Uncaught (?:\\w+)Error:")
    private val sourceRegex = Regex("https?://vk\\.com/")

    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        val hasJsError = jsErrorRegex.containsMatchIn(consoleMessage.message().orEmpty())
        val isVkSource = sourceRegex.containsMatchIn(consoleMessage.sourceId().orEmpty())
        if (hasJsError && isVkSource) {
            viewModel.notifyNewJsError()
        }
        return true
    }

    override fun onCreateWindow(
        view: WebView,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message,
    ): Boolean {
        val newWebView = WebView(view.context)
        AlertDialog.Builder(view.context)
            .setView(newWebView)
            .show()
        val transport = resultMsg.obj as WebView.WebViewTransport
        transport.webView = newWebView
        resultMsg.sendToTarget()
        return true
    }
}