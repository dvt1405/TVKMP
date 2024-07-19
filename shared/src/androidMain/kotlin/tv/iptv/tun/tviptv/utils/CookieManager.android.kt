package tv.iptv.tun.tviptv.utils

import android.webkit.CookieManager

actual fun getCookieForPage(url: String): String {
    return CookieManager.getInstance().getCookie(url)
}