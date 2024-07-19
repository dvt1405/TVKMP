package tv.iptv.tun.tviptv.repository

expect open class BrowserDataSource() {
    suspend fun getHtmlPage(url: String): String
    fun setOnDisableWebView()
}