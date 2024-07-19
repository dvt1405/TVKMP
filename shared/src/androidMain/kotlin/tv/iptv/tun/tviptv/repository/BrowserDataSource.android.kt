package tv.iptv.tun.tviptv.repository

import android.webkit.WebView

actual open class BrowserDataSource {
    private var webView: WebView? = null
    actual suspend fun getHtmlPage(url: String): String {
        TODO("Not yet implemented")
    }

    actual fun setOnDisableWebView() {
        webView?.onPause()
        webView?.destroy()
        webView = null
    }
}