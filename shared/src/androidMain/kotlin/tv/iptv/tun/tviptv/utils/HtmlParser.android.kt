package tv.iptv.tun.tviptv.utils

import org.jsoup.Jsoup

actual fun createHTMLParser(): HtmlParser {
    return object : HtmlParser {
        override fun parseWithCssSelector(html: String, selector: String): List<String> {
            return Jsoup.parse(html)
                .select(selector)
                .map {
                    it.html()
                }
        }

        override fun getElementByTag(html: String, tag: String): List<String> {
            return Jsoup.parse(html)
                .getElementsByTag(tag)
                .map {
                    it.html()
                }
        }

        override fun getElementById(html: String, id: String): String? {
            return Jsoup.parse(html)
                .getElementById(id)?.html()
        }
    }
}