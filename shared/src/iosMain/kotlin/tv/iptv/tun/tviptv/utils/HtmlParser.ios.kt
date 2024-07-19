package tv.iptv.tun.tviptv.utils

actual fun createHTMLParser(): HtmlParser {
    return object : HtmlParser {
        override fun parseWithCssSelector(html: String, selector: String): List<String> {
            return emptyList()
        }

        override fun getElementByTag(html: String, tag: String): List<String> {
            return emptyList()
        }

        override fun getElementById(html: String, id: String): String? {
            return null
        }
    }
}