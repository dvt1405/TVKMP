package tv.iptv.tun.tviptv.repository

actual open class BrowserDataSource {
    actual suspend fun getHtmlPage(url: String): String {
        TODO("Not yet implemented")
    }

    actual fun setOnDisableWebView() {
    }
}