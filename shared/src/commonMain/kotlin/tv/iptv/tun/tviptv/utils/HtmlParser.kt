package tv.iptv.tun.tviptv.utils

interface HtmlParser {
    fun parseWithCssSelector(html: String, selector: String): List<String>
    fun getElementByTag(html: String, tag: String): List<String>
    fun getElementById(html: String, id: String): String?
}

expect fun createHTMLParser(): HtmlParser

val htmlParser: HtmlParser by lazy { createHTMLParser() }